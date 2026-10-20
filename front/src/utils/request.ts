import axios from "axios"

/**
 * 后端 API 基础地址。
 * 通过环境变量 VITE_API_BASE_URL 覆盖（例如部署到服务器时），默认本地开发地址。
 * 其它地方请统一使用 resolveAssetUrl() 拼接图片/资源地址，不要硬编码 localhost。
 */
const BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined || "http://localhost:8080/api").replace(/\/$/, "");

/**
 * 把后端返回的相对资源路径（如 /images/xxx.jpg）拼成可直接访问的完整地址。
 */
export function resolveAssetUrl(path?: string | null): string {
	if (!path) return "";
	if (/^(https?:)?\/\//i.test(path) || path.startsWith("data:") || path.startsWith("blob:")) {
		return path;
	}
	return `${BASE_URL}${path.startsWith("/") ? "" : "/"}${path}`;
}

const request = axios.create({
	baseURL: BASE_URL,
	timeout: 30000
})

/** 防止并发请求同时触发多次跳转 */
let redirecting = false;

function handleUnauthorized(msg?: string) {
	if (redirecting) return;
	const onLoginPage = window.location.hash.includes("/login");
	localStorage.removeItem("token");
	localStorage.removeItem("userInfo");
	if (onLoginPage) return;
	redirecting = true;
	console.warn("[鉴权] 登录已失效：", msg || "未授权");
	// 使用 hash 路由跳转，并携带回跳地址
	const redirect = encodeURIComponent(window.location.hash.replace(/^#/, "") || "/timeline");
	window.location.hash = `/login?redirect=${redirect}&expired=1`;
	setTimeout(() => { redirecting = false; }, 1500);
}

// 请求拦截器：自动携带 token
request.interceptors.request.use(
	config => {
		const token = localStorage.getItem("token");
		if (token) {
			config.headers.Authorization = `Bearer ${token}`;
		}
		return config;
	},
	error => Promise.reject(error)
)

// 响应拦截器：统一错误处理
request.interceptors.response.use(
	response => response,
	error => {
		// 网络层错误（后端未启动 / 跨域失败）
		if (!error.response) {
			console.error("[网络错误] 请检查后端服务是否启动", error.message);
			return Promise.reject({ code: 500, msg: "网络错误，无法连接服务器，请检查后端是否已启动" });
		}

		const { status, data } = error.response;
		const msg = (data && data.msg) || `请求失败（HTTP ${status}）`;

		if (status === 401) {
			handleUnauthorized(msg);
			return Promise.reject({ code: 401, msg: msg || "登录已过期，请重新登录" });
		}
		if (status === 403) {
			console.warn("[权限不足]", msg);
			return Promise.reject({ code: 403, msg: msg || "权限不足" });
		}

		console.error("[接口错误]", status, msg);
		return Promise.reject({ code: data?.code ?? status, msg });
	}
)

/** 从任意异常对象中提取可展示的错误信息 */
export function errorMessage(err: any, fallback = "操作失败，请稍后重试"): string {
	if (!err) return fallback;
	if (typeof err === "string") return err;
	return err.msg || err.message || (err.response?.data?.msg) || fallback;
}

export { BASE_URL }
export default request
