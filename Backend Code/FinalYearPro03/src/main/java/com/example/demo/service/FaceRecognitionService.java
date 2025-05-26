package com.example.demo.service;

import com.example.demo.model.FaceData;
import com.example.demo.repository.FaceDataRepository;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class FaceRecognitionService {

    private CascadeClassifier faceDetector;
    private Net faceNet;
    private Net recognitionNet;
    private final Map<String, float[]> faceEmbeddings = new HashMap<>();
    // Adjusted threshold value: increased from 0.6f to 0.8f to allow more matches
    private final float RECOGNITION_THRESHOLD = 0.8f;
    private final FaceDataRepository faceDataRepository;
    
    // Paths to model files
    private final String FACE_DETECTION_MODEL = "models/deploy.prototxt";
    private final String FACE_DETECTION_WEIGHTS = "models/res10_300x300_ssd_iter_140000.caffemodel";
    private final String FACE_RECOGNITION_MODEL = "models/openface.nn4.small2.v1.t7";
    
    @Autowired
    public FaceRecognitionService(FaceDataRepository faceDataRepository) {
        this.faceDataRepository = faceDataRepository;
        
        // Make sure model directory exists
        try {
            Path modelDir = Paths.get("models");
            if (!Files.exists(modelDir)) {
                Files.createDirectories(modelDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create model directory", e);
        }
        
        // Initialize face detection cascade as backup
        faceDetector = loadCascadeClassifier("/haarcascade_frontalface_alt.xml");
        
        // Load pre-trained models
        loadModels();
    }
    
    /**
     * Loads pre-trained deep learning models for face detection and recognition.
     */
    private void loadModels() {
        try {
            // Load SSD face detector
            File protoFile = loadResourceToFile("/models/deploy.prototxt.txt", FACE_DETECTION_MODEL);
            File modelFile = loadResourceToFile("/models/res10_300x300_ssd_iter_140000.caffemodel", FACE_DETECTION_WEIGHTS);
            
            faceNet = opencv_dnn.readNetFromCaffe(
                protoFile.getAbsolutePath(),
                modelFile.getAbsolutePath()
            );
            
            // Load OpenFace model for face embedding
            File faceRecognizerFile = loadResourceToFile("/models/openface.nn4.small2.v1.t7", FACE_RECOGNITION_MODEL);
            recognitionNet = opencv_dnn.readNetFromTorch(faceRecognizerFile.getAbsolutePath());
            
            System.out.println("Deep learning models loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load deep learning models: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a resource file from classpath to a file on disk.
     */
    private File loadResourceToFile(String resourcePath, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        
        // Skip if file already exists
        if (outputFile.exists()) {
            return outputFile;
        }
        
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            
            outputFile.getParentFile().mkdirs();
            try (FileOutputStream os = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
        return outputFile;
    }

    /**
     * Loads a cascade classifier file from the classpath.
     */
    private CascadeClassifier loadCascadeClassifier(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            File tempFile = File.createTempFile("cascade", ".xml");
            tempFile.deleteOnExit();
            try (FileOutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            return new CascadeClassifier(tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load cascade classifier from " + resourcePath, e);
        }
    }

    @PostConstruct
    public void init() {
        loadFaceEmbeddings();
    }

    /**
     * Loads face embeddings from database into memory.
     */
    private void loadFaceEmbeddings() {
        List<FaceData> faceDataList = faceDataRepository.findAll();
        for (FaceData data : faceDataList) {
            if (data.getFaceEmbedding() != null) {
                faceEmbeddings.put(data.getUsername(), data.getFaceEmbedding());
            }
        }
        System.out.println("Loaded " + faceEmbeddings.size() + " face embeddings");
    }

    /**
     * Detects faces in an image using a deep learning model.
     */
    private List<Rect> detectFacesWithDNN(Mat image) {
        List<Rect> faceRects = new ArrayList<>();
        
        try {
            Mat inputBlob = opencv_dnn.blobFromImage(
                    image,
                    1.0,
                    new Size(300, 300),
                    new Scalar(104.0, 177.0, 123.0, 0.0),
                    false,
                    false,
                    opencv_core.CV_32F  // Force output blob depth to CV_32F
            );
            faceNet.setInput(inputBlob);
            
            // Forward pass to get face detections
            Mat detections = faceNet.forward();
            
            // The detection output is a 4D blob [1,1,N,7]
            int numDetections = (int) detections.size(2);
            int height = image.rows();
            int width = image.cols();
            FloatIndexer idx = detections.createIndexer();
            
            for (int i = 0; i < numDetections; i++) {
                float confidence = idx.get(0, 0, i, 2);
                if (confidence > 0.5) {
                    float x1 = idx.get(0, 0, i, 3) * width;
                    float y1 = idx.get(0, 0, i, 4) * height;
                    float x2 = idx.get(0, 0, i, 5) * width;
                    float y2 = idx.get(0, 0, i, 6) * height;
                    
                    Rect faceRect = new Rect(
                            (int) x1,
                            (int) y1,
                            (int) (x2 - x1),
                            (int) (y2 - y1)
                    );
                    faceRects.add(faceRect);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in face detection: " + e.getMessage());
            e.printStackTrace();
        }
        
        return faceRects;
    }
    
    /**
     * Fallback method to detect faces using cascade classifier.
     */
    private List<Rect> detectFacesWithCascade(Mat image) {
        List<Rect> faceRects = new ArrayList<>();
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);
        
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(gray, faces);
        
        for (long i = 0; i < faces.size(); i++) {
            faceRects.add(faces.get(i));
        }
        
        return faceRects;
    }
    
    /**
     * Extracts face embeddings using the deep neural network.
     */
    private float[] extractFaceEmbedding(Mat faceImg) {
        try {
            // Resize face to 96x96 as expected by the OpenFace model
            Mat resizedFace = new Mat();
            opencv_imgproc.resize(faceImg, resizedFace, new Size(96, 96));
            
            // Create a blob from the resized face.
            Mat faceBlob = opencv_dnn.blobFromImage(
                    resizedFace,
                    1.0 / 255.0,
                    new Size(96, 96),
                    new Scalar(0.0, 0.0, 0.0, 0.0),
                    true,
                    false,
                    opencv_core.CV_32F  // Force output blob depth to CV_32F
            );
            recognitionNet.setInput(faceBlob);
            Mat embedding = recognitionNet.forward();
            
            // Convert embedding to a float array
            float[] embeddingArray = new float[(int) embedding.total()];
            FloatIndexer embeddingIdx = embedding.createIndexer();
            for (int i = 0; i < embeddingArray.length; i++) {
                embeddingArray[i] = embeddingIdx.get(0, i);
            }
            
            // Normalize the embedding (L2 normalization)
            float sum = 0;
            for (float val : embeddingArray) {
                sum += val * val;
            }
            sum = (float) Math.sqrt(sum);
            if(sum == 0) {
                System.err.println("Warning: Embedding norm is zero.");
                return null;
            }
            for (int i = 0; i < embeddingArray.length; i++) {
                embeddingArray[i] /= sum;
            }
            
            return embeddingArray;
        } catch (Exception e) {
            System.err.println("Error extracting face embedding: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registers a new user with their face image and personal details.
     */
    public String registerFace(String userName, String name, long mobNo, String dept, String college,
                               String collegeUsername, int age, String password, MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        Mat rawData = new Mat(1, fileBytes.length, opencv_core.CV_8UC1, new BytePointer(fileBytes));
        Mat image = opencv_imgcodecs.imdecode(rawData, opencv_imgcodecs.IMREAD_COLOR);
        
        if (image.empty()) {
            throw new IOException("Failed to load image");
        }
        
        // Try to detect faces with DNN first
        List<Rect> faces = detectFacesWithDNN(image);
        
        // Fall back to cascade classifier if no faces detected
        if (faces.isEmpty()) {
            faces = detectFacesWithCascade(image);
            if (faces.isEmpty()) {
                throw new IOException("No face detected in the image");
            }
        }
        
        Rect faceRect = faces.get(0); // Use the first detected face
        Mat face = new Mat(image, faceRect);
        
        // Extract face embedding
        float[] embedding = extractFaceEmbedding(face);
        if (embedding == null) {
            throw new IOException("Failed to extract face features");
        }
        
        // Save face image as bytes
        BytePointer buf = new BytePointer();
        opencv_imgcodecs.imencode(".png", face, buf);
        byte[] faceBytes = new byte[(int) buf.limit()];
        buf.get(faceBytes);
        
        // Store user data with embedding
        FaceData faceData = new FaceData(userName, name, mobNo, dept, college, collegeUsername, age, password, faceBytes);
        faceData.setFaceEmbedding(embedding);
        faceDataRepository.save(faceData);
        
        // Update in-memory map
        faceEmbeddings.put(userName, embedding);
        
        return "User " + userName + " registered successfully.";
    }

    /**
     * Recognizes a face from an image file.
     */
    public String recognizeFace(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        Mat rawData = new Mat(1, fileBytes.length, opencv_core.CV_8UC1, new BytePointer(fileBytes));
        Mat image = opencv_imgcodecs.imdecode(rawData, opencv_imgcodecs.IMREAD_COLOR);
        
        if (image.empty()) {
            throw new IOException("Failed to load image");
        }
        
        // Try to detect faces with DNN first
        List<Rect> faces = detectFacesWithDNN(image);
        
        // Fall back to cascade classifier if no faces detected
        if (faces.isEmpty()) {
            faces = detectFacesWithCascade(image);
            if (faces.isEmpty()) {
                throw new IOException("No face detected in the image");
            }
        }
        
        Rect faceRect = faces.get(0); // Use the first detected face
        Mat face = new Mat(image, faceRect);
        
        // Extract face embedding
        float[] queryEmbedding = extractFaceEmbedding(face);
        if (queryEmbedding == null) {
            throw new IOException("Failed to extract face features");
        }
        
        // Find the closest match
        String bestMatch = null;
        float bestDistance = Float.MAX_VALUE;
        
        for (Map.Entry<String, float[]> entry : faceEmbeddings.entrySet()) {
            float distance = calculateDistance(queryEmbedding, entry.getValue());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestMatch = entry.getKey();
            }
        }
        
        // Check if the match is close enough (using the adjusted threshold)
        if (bestMatch != null && bestDistance < RECOGNITION_THRESHOLD) {
            return bestMatch;
        } else {
            throw new IOException("Face not recognized (confidence too low). Best distance: " + bestDistance);
        }
    }
    
    /**
     * Calculate Euclidean distance between two face embeddings.
     */
    private float calculateDistance(float[] embedding1, float[] embedding2) {
        float sum = 0;
        for (int i = 0; i < embedding1.length; i++) {
            float diff = embedding1[i] - embedding2[i];
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }
}
