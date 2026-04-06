import axios from "axios"

const BASE_URL = "http://localhost:8080/api";

const request = axios.create({
	baseURL: BASE_URL,
	timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
	config => {
		const token = localStorage.getItem('token');
		if (token) {
			config.headers.Authorization = `Bearer ${token}`;
		}
		console.log('[Request]', config.method?.toUpperCase(), config.url);
		return config;
	},
	error => {
		console.error('[Request Error]', error);
		return Promise.reject(error);
	}
)

// 响应拦截器 - 修改这里
request.interceptors.response.use(
	response => {
		console.log('[Response]', response.config.url, response.data);
		return response;
	},
	error => {
		if (error.response) {
			console.error('[Response Error]', error.response.status, error.response.data);
			return Promise.reject(error.response);
		} else if (error.request) {
			console.error('[Network Error] 请检查后端服务是否启动');
			return Promise.reject({ code: 500, msg: '网络错误，请检查后端服务' });
		} else {
			console.error('[Request Error]', error.message);
			return Promise.reject({ code: 500, msg: error.message });
		}
	}
)

export { BASE_URL }
export default request