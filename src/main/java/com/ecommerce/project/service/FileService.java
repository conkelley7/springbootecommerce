package com.ecommerce.project.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service interface for handling file upload operations.
 * Provides method to upload files or images to a specified location.
 */
public interface FileService {
    /**
     * Uploads an image file to the specified path on the server.
     * The image file is saved with a unique name to avoid overwriting existing files.
     *
     * @param path the directory path where the file will be uploaded
     * @param file the image file to be uploaded, provided as a MultipartFile
     * @return the unique name of the uploaded file for further reference
     * @throws IOException if an I/O error occurs during file upload
     */
    String uploadImage(String path, MultipartFile file) throws IOException;
}
