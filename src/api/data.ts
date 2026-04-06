import request from "@/utils/request";

export const backupData = () => {
    return request.post("/data/backup");
}

export const recoverData = (filename: string) => {
    return request.post("/data/recover", {}, { 
        params: { filename } 
    });
}

export const getFileList = () => {
    return request.get("/data/list");
}

export const deleteData = (filename: string) => {
    return request.delete("/data/delete", { 
        params: { filename } 
    });
}