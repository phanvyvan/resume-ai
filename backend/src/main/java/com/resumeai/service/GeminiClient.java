package com.resumeai.service;
// GeminiClient - Helper gọi Gemini API dùng chung
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.resumeai.dto.ResumeAnalysisDTO;

import autovalue.shaded.org.checkerframework.checker.nullness.qual.Nullable;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiClient {
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String modelName;

    private final Gson gson = new Gson();

    public GenerateContentResponse generateContent(List<Content> contents, Schema schema) {
        try (Client geminiClient = Client.builder().apiKey(apiKey).build()) {
            GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();
            
            // Chỉ set schema nếu không null
            if (schema != null) {
                configBuilder.responseMimeType("application/json")
                           .responseSchema(schema);
            }
            
            GenerateContentConfig config = configBuilder.build();
            return geminiClient.models.generateContent(modelName, contents, config);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi API Gemini: " + e.getMessage(), e);
        }
    }

    public ResumeAnalysisDTO analyzeResume(String rawResumeText, @Nullable String jobDescription) {
        try {
            // Tạo prompt chi tiết cho Gemini
            String prompt;
            
       if (jobDescription != null && !jobDescription.trim().isEmpty()) {
            prompt = String.format("""
                Bạn là một chuyên gia tuyển dụng và hướng nghiệp với nhiều năm kinh nghiệm.

                Đây là một đoạn văn bản hồ sơ xin việc chưa được định dạng:
                ---
                %s
                ---

                Đây là mô tả công việc tương ứng:
                ---
                %s
                ---

                Hãy phân tích hồ sơ dựa trên MÔ TẢ CÔNG VIỆC và trích xuất các phần: "Kinh nghiệm làm việc", "Học vấn", và "Kỹ năng". 
                Với mỗi phần, hãy:
                1. Trích xuất và tóm tắt nội dung hiện tại từ hồ sơ
                2. Đánh giá mức độ phù hợp của phần này so với mô tả công việc (ghi rõ: phù hợp / chưa phù hợp)
                3. Đề xuất chỉnh sửa/bổ sung để nâng cao mức độ phù hợp
                4. Giải thích lý do tại sao cần những đề xuất đó

                Nếu phần nào bị thiếu, hãy ghi rõ "Không có thông tin" trong nội dung.

                Trả kết quả theo định dạng JSON sau:
                {
                  "kinh_nghiem_lam_viec": {
                    "noi_dung": "...",
                    "de_xuat": "...",
                    "ly_do": "..."
                  },
                  "hoc_van": {
                    "noi_dung": "...",
                    "de_xuat": "...",
                    "ly_do": "..."
                  },
                  "ky_nang": {
                    "noi_dung": "...",
                    "de_xuat": "...",
                    "ly_do": "..."
                  }
                }
                """, rawResumeText, jobDescription);
        } else {
            // Giữ nguyên logic cũ nếu không có job description
            prompt = String.format("""
                Bạn là một chuyên gia tuyển dụng và hướng nghiệp với nhiều năm kinh nghiệm.

                Đây là một đoạn văn bản hồ sơ xin việc chưa được định dạng:
                ---
                %s
                ---

                Hãy phân tích đoạn văn này và trích xuất các phần: "Kinh nghiệm làm việc", "Học vấn", và "Kỹ năng". 
                Với mỗi phần, hãy:
                1. Trích xuất và tóm tắt nội dung hiện tại từ văn bản gốc
                2. Đề xuất nội dung cụ thể nên bổ sung hoặc chỉnh sửa để cải thiện hồ sơ
                3. Giải thích rõ ràng lý do tại sao cần có những đề xuất này

                Nếu phần nào thiếu hoàn toàn, hãy ghi "Không có thông tin" trong noi_dung.

                Trả kết quả theo định dạng JSON sau:
                {
                  "kinh_nghiem_lam_viec": {
                    "noi_dung": "...",
                    "de_xuat": "...",
                    "ly_do": "..."
                  },
                  "hoc_van": {
                    "noi_dung": "...",
                    "de_xuat": "...",
                    "ly_do": "..."
                  },
                  "ky_nang": {
                    "noi_dung": "...",
                    "de_xuat": "...",
                    "ly_do": "..."
                  }
                }
                """, rawResumeText);
        }
            

            // Tạo Content với builder pattern
            Content content = Content.builder()
                .role("user")
                .parts(List.of(Part.fromText(prompt)))
                .build();
            
            // Gọi API Gemini với schema null (để Gemini tự format JSON)
            GenerateContentResponse response = generateContent(List.of(content), null);
            
            // Trích xuất JSON từ response
            String responseText = response.text();
            if (responseText.contains("```json")) {
                responseText = responseText.replace("```json", "").trim();
            }
            if (responseText.contains("```")) {
                responseText = responseText.replace("```", "").trim();
            }

            ObjectMapper mapper = new ObjectMapper();
            ResumeAnalysisDTO dto = mapper.readValue(responseText, ResumeAnalysisDTO.class);

            return dto;
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Lỗi khi phân tích hồ sơ với Gemini: " + e.getMessage(), e);
        }
    }

    // public ResumeAnalysisDTO analyzeResumeToDTO(String rawResumeText) {
    //     try {
    //         JsonObject jsonResult = analyzeResume(rawResumeText);
    //         return gson.fromJson(jsonResult, ResumeAnalysisDTO.class);
    //     } catch (Exception e) {
    //         throw new RuntimeException("Lỗi khi convert JsonObject sang DTO: " + e.getMessage(), e);
    //     }
    // }

    // DTOs cho kết quả phân tích
    public static class ResumeAnalysisResult {
        private SectionAnalysis kinh_nghiem_lam_viec;
        private SectionAnalysis hoc_van;
        private SectionAnalysis ky_nang;

        // Getters and setters
        public SectionAnalysis getKinh_nghiem_lam_viec() { return kinh_nghiem_lam_viec; }
        public void setKinh_nghiem_lam_viec(SectionAnalysis kinh_nghiem_lam_viec) { this.kinh_nghiem_lam_viec = kinh_nghiem_lam_viec; }
        
        public SectionAnalysis getHoc_van() { return hoc_van; }
        public void setHoc_van(SectionAnalysis hoc_van) { this.hoc_van = hoc_van; }
        
        public SectionAnalysis getKy_nang() { return ky_nang; }
        public void setKy_nang(SectionAnalysis ky_nang) { this.ky_nang = ky_nang; }
    }

    public static class SectionAnalysis {
        private String noi_dung;
        private String de_xuat;
        private String ly_do;

        // Getters and setters
        public String getNoi_dung() { return noi_dung; }
        public void setNoi_dung(String noi_dung) { this.noi_dung = noi_dung; }
        
        public String getDe_xuat() { return de_xuat; }
        public void setDe_xuat(String de_xuat) { this.de_xuat = de_xuat; }
        
        public String getLy_do() { return ly_do; }
        public void setLy_do(String ly_do) { this.ly_do = ly_do; }
    }
}