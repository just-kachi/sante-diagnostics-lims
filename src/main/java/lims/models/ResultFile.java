package lims.models;

import java.time.LocalDateTime;

public class ResultFile {

    private int id;
    private int resultId;
    private String fileName;
    private String filePath;
    private String fileType;
    private LocalDateTime uploadedAt;

    public ResultFile() {
    }

    public ResultFile(int id, int resultId, String fileName, String filePath,
                      String fileType, LocalDateTime uploadedAt) {
        this.id = id;
        this.resultId = resultId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
    }

    public int getId() {
        return id;
    }

    public int getResultId() {
        return resultId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}