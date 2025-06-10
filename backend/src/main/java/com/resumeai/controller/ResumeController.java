package com.resumeai.controller;

import com.resumeai.model.ResumeData;
import com.resumeai.dto.ResumeAnalysisDTO;
import com.resumeai.model.InterviewQuestion;
import com.resumeai.model.JobMatchResult;
import com.resumeai.service.DocumentParserService;
import com.resumeai.service.GeminiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/resume")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ResumeController {

   @Autowired
   private DocumentParserService documentParserService;

   @Autowired
   private GeminiClient geminiService;

   @PostMapping("/upload")
   public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
       try {
           // Validate file
           if (file.isEmpty()) {
               return ResponseEntity.badRequest()
                   .body(Map.of("error", "File không được để trống"));
           }

           if (!documentParserService.isValidFileType(file.getOriginalFilename())) {
               return ResponseEntity.badRequest()
                   .body(Map.of("error", "Chỉ hỗ trợ file PDF, DOC, DOCX"));
           }

           if (file.getSize() > documentParserService.getMaxFileSizeInBytes()) {
               return ResponseEntity.badRequest()
                   .body(Map.of("error", "File quá lớn (tối đa 10MB)"));
           }

           // Extract text from file only - no Gemini analysis yet
           String extractedText = documentParserService.extractTextFromFile(file);

           if (extractedText == null || extractedText.trim().isEmpty()) {
               return ResponseEntity.badRequest()
                   .body(Map.of("error", "Không thể trích xuất text từ file. File có thể là PDF dạng scan hoặc không chứa text."));
           }

           // Check if text is too short (possible scan)
           if (extractedText.trim().length() < 50) {
               return ResponseEntity.badRequest()
                   .body(Map.of("error", "Nội dung text quá ngắn. File có thể là PDF dạng scan chứ không phải dạng text. Điều này có thể bị loại bởi hệ thống lọc CV tự động. Vui lòng chuyển CV thành dạng text trước khi upload."));
           }

           Map<String, Object> response = new HashMap<>();
           response.put("success", true);
           response.put("message", "Trích xuất text thành công");
           response.put("extractedText", extractedText);
           response.put("filename", file.getOriginalFilename());

           return ResponseEntity.ok(response);

       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body(Map.of("error", "Lỗi xử lý file: " + e.getMessage()));
       }
   }

   @PostMapping("/analyze-text")
   public ResponseEntity<?> analyzeResumeText(@RequestBody Map<String, String> request) {
        try {
            String resumeText = request.get("text");
            String jobDescription = request.getOrDefault("jobDescription", null);

            if (resumeText == null || resumeText.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Text CV không được để trống"));
            }

            ResumeAnalysisDTO result = geminiService.analyzeResume(resumeText, jobDescription);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Phân tích CV thành công",
                "data", result
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi phân tích CV: " + e.getMessage()));
        }
   }

//    @PostMapping("/job-match")
//    public ResponseEntity<?> analyzeJobMatch(@RequestBody Map<String, String> request) {
//        try {
//            String jobDescription = request.get("jobDescription");
//            String resumeText = request.get("resumeText");

//            if (jobDescription == null || jobDescription.trim().isEmpty()) {
//                return ResponseEntity.badRequest()
//                    .body(Map.of("error", "Mô tả công việc không được để trống"));
//            }

//            if (resumeText == null || resumeText.trim().isEmpty()) {
//                return ResponseEntity.badRequest()
//                    .body(Map.of("error", "Text CV không được để trống"));
//            }

//            // Calculate job match score
//            double score = geminiService.calculateJobMatch(jobDescription, resumeText);

//            JobMatchResult result = new JobMatchResult();
//            result.setScore(score);
//            result.setAnalysis("Phân tích độ phù hợp dựa trên AI");
//            result.setStrengths("Điểm mạnh được xác định từ CV");
//            result.setImprovements("Gợi ý cải thiện để phù hợp hơn với công việc");

//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("data", result);

//            return ResponseEntity.ok(response);

//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("error", "Lỗi phân tích job match: " + e.getMessage()));
//        }
//    }

//    @PostMapping("/interview-questions")
//    public ResponseEntity<?> generateInterviewQuestions(@RequestBody Map<String, String> request) {
//        try {
//            String jobDescription = request.get("jobDescription");
//            String resumeText = request.get("resumeText");

//            // Generate questions using Gemini AI
//            List<String> questionTexts = geminiService.generateInterviewQuestions(jobDescription, resumeText);

//            // Convert to InterviewQuestion objects with duration
//            List<InterviewQuestion> questions = IntStream.range(0, questionTexts.size())
//                .mapToObj(i -> new InterviewQuestion(i + 1, questionTexts.get(i), 120)) // 2 minutes per question
//                .toList();

//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("data", questions);

//            return ResponseEntity.ok(response);

//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("error", "Lỗi tạo câu hỏi phỏng vấn: " + e.getMessage()));
//        }
//    }

//    @GetMapping("/health")
//    public ResponseEntity<?> healthCheck() {
//        Map<String, String> response = new HashMap<>();
//        response.put("status", "OK");
//        response.put("message", "Resume AI Backend đang hoạt động");
//        return ResponseEntity.ok(response);
//    }
}