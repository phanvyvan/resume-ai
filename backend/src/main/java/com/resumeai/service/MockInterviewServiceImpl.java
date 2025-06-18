package com.resumeai.service;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.resumeai.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MockInterviewServiceImpl implements MockInterviewService {

    private static final Logger logger = LoggerFactory.getLogger(MockInterviewServiceImpl.class);

    @Autowired
    private GeminiClient geminiClient;

    private final Gson gson = new Gson();

    @Override
    public List<InterviewQuestionDTO> generateQuestions(InterviewRequestDTO request) {
        if (request == null || request.getPosition() == null || request.getField() == null || request.getLevel() == null) {
            logger.error("Invalid InterviewRequestDTO: null or missing fields");
            return new ArrayList<>();
        }

        try {
            // Create prompt for Gemini
            String prompt = buildQuestionGenerationPrompt(request);

            // Create schema for structured output
            Schema questionSchema = createQuestionSchema();

            // Create content for Gemini
            List<Content> contents = List.of(
                    Content.builder()
                            .parts(List.of(Part.fromText(prompt)))
                            .build()
            );

            // Call Gemini API
            GenerateContentResponse response = geminiClient.generateContent(contents, questionSchema);

            // Extract JSON from response
            String jsonResponse = response.text();

            // Parse JSON response into List<InterviewQuestionDTO>
            java.lang.reflect.Type listType = new TypeToken<List<InterviewQuestionDTO>>(){}.getType();
            List<InterviewQuestionDTO> questions = gson.fromJson(jsonResponse, listType);

            return questions != null ? questions : new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error generating questions: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo câu hỏi phỏng vấn: " + e.getMessage(), e);
        }
    }

    @Override
    public GradingResultDTO gradeAnswers(AnswerSubmitDTO submission) {
        if (submission == null || submission.getAnswers() == null || submission.getAnswers().isEmpty() ||
                submission.getPosition() == null || submission.getField() == null || submission.getLevel() == null) {
            logger.error("Invalid AnswerSubmitDTO: null or missing fields/answers");
            return new GradingResultDTO(0, new ArrayList<>(), "Dữ liệu đầu vào không hợp lệ", "Vui lòng cung cấp đầy đủ thông tin và câu trả lời.");
        }

        try {
            // Create prompt for grading answers
            String prompt = buildGradingPrompt(submission);

            // Create schema for structured output
            Schema gradingSchema = createGradingSchema();

            // Create content for Gemini
            List<Content> contents = List.of(
                    Content.builder()
                            .parts(List.of(Part.fromText(prompt)))
                            .build()
            );

            // Call Gemini API
            GenerateContentResponse response = geminiClient.generateContent(contents, gradingSchema);

            // Extract JSON from response
            String jsonResponse = response.text();

            // Parse JSON response into GradingResultDTO
            java.lang.reflect.Type gradingType = new TypeToken<GradingResultDTO>(){}.getType();
            GradingResultDTO result = gson.fromJson(jsonResponse, gradingType);

            return result != null ? result : new GradingResultDTO(0, new ArrayList<>(), "Không thể chấm điểm", "Vui lòng thử lại sau.");
        } catch (Exception e) {
            logger.error("Error grading answers: {}", e.getMessage(), e);
            return new GradingResultDTO(0, new ArrayList<>(), "Lỗi khi chấm điểm", "Vui lòng thử lại sau.");
        }
    }

    private Schema createQuestionSchema() {
        return Schema.builder()
                .type(Type.Known.ARRAY)
                .items(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(ImmutableMap.of(
                                "questionId", Schema.builder().type(Type.Known.INTEGER).build(),
                                "questionText", Schema.builder().type(Type.Known.STRING).build(),
                                "hint", Schema.builder().type(Type.Known.STRING).build()
                        ))
                        .required(List.of("questionId", "questionText", "hint"))
                        .build())
                .build();
    }

    private Schema createGradingSchema() {
        return Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(ImmutableMap.of(
                        "totalScore", Schema.builder().type(Type.Known.INTEGER).build(),
                        "questionScores", Schema.builder()
                                .type(Type.Known.ARRAY)
                                .items(Schema.builder()
                                        .type(Type.Known.OBJECT)
                                        .properties(ImmutableMap.of(
                                                "questionId", Schema.builder().type(Type.Known.INTEGER).build(),
                                                "score", Schema.builder().type(Type.Known.INTEGER).build(),
                                                "feedback", Schema.builder().type(Type.Known.STRING).build()
                                        ))
                                        .required(List.of("questionId", "score", "feedback"))
                                        .build())
                                .build(),
                        "generalFeedback", Schema.builder().type(Type.Known.STRING).build(),
                        "improvementSuggestions", Schema.builder().type(Type.Known.STRING).build()
                ))
                .required(List.of("totalScore", "questionScores", "generalFeedback", "improvementSuggestions"))
                .build();
    }

    private String buildQuestionGenerationPrompt(InterviewRequestDTO request) {
        // Prompt này giữ nguyên logic tư duy nâng cao nhưng tuân thủ nghiêm ngặt định dạng output gốc.
        return String.format(
                "Bạn là một chuyên gia tuyển dụng và phỏng vấn kỹ thuật hàng đầu, có kinh nghiệm sâu sắc về các tiêu chuẩn nhân sự trong ngành. " +
                        "Mục tiêu chính của bạn là tạo ra một bộ 10 câu hỏi phỏng vấn để đánh giá chính xác năng lực của một ứng viên so với **mặt bằng chung của thị trường** cho vị trí và cấp độ được chỉ định.\n\n" +
                        "**THÔNG TIN ỨNG VIÊN:**\n\n" +
                        "- Vị trí ứng tuyển: %s\n" +
                        "- Lĩnh vực công việc: %s\n" +
                        "- Cấp độ kinh nghiệm: %s\n\n" +
                        "**YÊU CẦU CHI TIẾT:**\n\n" +
                        "1.  **Suy luận kỳ vọng thị trường:** Dựa vào 3 thông tin trên, hãy tự suy luận ra những trách nhiệm, công nghệ cốt lõi và thách thức phổ biến mà một người ở vai trò này thường gặp phải trên thị trường. Câu hỏi của bạn phải dựa trên những suy luận này.\n\n" +
                        "2.  **Mục tiêu cốt lõi:** Các câu hỏi phải giúp phân biệt được một ứng viên 'đạt yêu cầu' và một ứng viên 'xuất sắc' cho cấp độ %s. Hãy tập trung vào việc kiểm tra chiều sâu kiến thức và kinh nghiệm thực tiễn.\n\n" +
                        "3.  **Kết hợp câu hỏi (Tỷ lệ gợi ý: 40%% Technical, 40%% Behavioral, 20%% Situational):** Hãy ngầm hiểu và tạo ra sự đa dạng này mà không cần nêu rõ loại câu hỏi trong output. Tập trung vào việc tạo ra câu hỏi thực tế, đánh giá đúng năng lực.\n\n" +
                        "4.  **Gợi ý trả lời (Hint):** Hint không được tiết lộ đáp án. Thay vào đó, hãy gợi ý cho ứng viên về *cách tiếp cận vấn đề* hoặc *cấu trúc một câu trả lời tốt* (ví dụ: 'Hãy trình bày theo mô hình STAR' hoặc 'Hãy cân nhắc các yếu tố về hiệu năng và khả năng mở rộng').\n\n" +
                        "5.  **Ngôn ngữ:** Câu hỏi bằng tiếng Việt, chuyên nghiệp, rõ ràng.\n\n" +
                        "**ĐỊNH DẠNG ĐẦU RA:**\n\n" +
                        "Hãy trả về kết quả theo định dạng JSON array như mẫu sau. **Tuyệt đối không thêm bất kỳ trường nào khác ngoài `questionId`, `questionText`, và `hint`.**\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"questionId\": 1,\n" +
                        "    \"questionText\": \"Câu hỏi phỏng vấn...\",\n" +
                        "    \"hint\": \"Gợi ý trả lời...\"\n" +
                        "  }\n" +
                        "]\n",
                request.getPosition(),
                request.getField(),
                request.getLevel(),
                request.getLevel()
        );
    }


    private String buildGradingPrompt(AnswerSubmitDTO submission) {
        String answersText = submission.getAnswers().stream()
                .map(answer -> String.format(
                        "- Câu hỏi ID: %d\n  Nội dung câu hỏi: %s\n  Câu trả lời: %s",
                        answer.getQuestionId(),
                        answer.getQuestionText(),
                        answer.getAnswerText()))
                .collect(Collectors.joining("\n\n"));

        return """
                Bạn là một chuyên gia phỏng vấn có kinh nghiệm thực tế tuyển dụng nhân sự. Hãy đóng vai trò là nhà tuyển dụng đang đánh giá ứng viên cho vị trí "%s" trong lĩnh vực "%s" với cấp độ kinh nghiệm "%s".
            
                Nhiệm vụ của bạn là chấm điểm và phản hồi các câu trả lời phỏng vấn như sau:
            
                1. Đối với mỗi câu trả lời:
                   - Nếu là câu hỏi hành vi (behavioral), hãy đánh giá dựa theo mô hình STAR: ứng viên có mô tả rõ tình huống (Situation), nhiệm vụ (Task), hành động (Action), và kết quả (Result) hay không.
                   - Nếu là câu hỏi chuyên môn (technical), hãy đánh giá theo độ chính xác, chiều sâu, tính logic và khả năng áp dụng thực tế.
                   - Chấm điểm mỗi câu từ 0 đến 100.
                   - Viết phần nhận xét `feedback` tổng hợp: nêu rõ điểm mạnh, điểm yếu và cách cải thiện cho câu trả lời đó.
            
                2. Sau khi đánh giá tất cả các câu hỏi:
                   - Tính `totalScore` là trung bình cộng của điểm các câu (làm tròn đến số nguyên).
                   - Viết nhận xét chung `generalFeedback` về màn thể hiện tổng thể của ứng viên.
                   - Gợi ý cải thiện `improvementSuggestions` để giúp ứng viên thể hiện tốt hơn trong các buổi phỏng vấn sau.
            
                3. Kết quả trả về phải có định dạng JSON như sau (không thêm bớt trường):
            
                {
                  "totalScore": 85,
                  "questionScores": [
                    {
                      "questionId": 1,
                      "score": 80,
                      "feedback": "Câu trả lời tốt, trình bày mạch lạc, tuy nhiên cần bổ sung ví dụ minh họa rõ hơn để làm nổi bật kết quả đạt được."
                    }
                  ],
                  "generalFeedback": "Ứng viên thể hiện khá tốt, có tư duy rõ ràng, nhưng nên bổ sung thêm dẫn chứng cụ thể.",
                  "improvementSuggestions": "Nên luyện cách trả lời theo STAR và chuẩn bị ví dụ minh họa rõ ràng hơn cho các câu hỏi."
                }
            
                Tất cả phản hồi phải viết bằng tiếng Việt, dễ hiểu và chuyên nghiệp.
            
                Danh sách câu hỏi và câu trả lời:
                %s
                """
                .formatted(
                submission.getPosition(),
                submission.getField(),
                submission.getLevel(),
                answersText
        );
    }

}