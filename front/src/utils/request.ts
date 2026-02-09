import axios from "axios"

const BASE_URL = "http://localhost:8080/api";
export { BASE_URL }
const request = axios.create({
	baseURL: BASE_URL
})

export default request;