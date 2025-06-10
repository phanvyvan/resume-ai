// API service to communicate with Java backend

const API_BASE_URL = 'http://localhost:8080/api';

interface ResumeAnalysis {
  personalInfo: {
    name: string;
    email: string;
    phone: string;
    address: string;
  };
  sections: Array<{
    title: string;
    content: string;
    improvements: string[];
  }>;
  extractedText?: string;
}

interface InterviewQuestion {
  id: number;
  question: string;
  expectedDuration: number; // in seconds
}

interface JobMatchResult {
  score: number;
  analysis: string;
  strengths: string;
  improvements: string;
}

// Function to extract text from uploaded file (no Gemini analysis)
export const extractTextFromFile = async (file: File): Promise<{extractedText: string, filename: string}> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_BASE_URL}/resume/upload`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Lỗi upload file');
  }

  const result = await response.json();
  return {
    extractedText: result.extractedText,
    filename: result.filename
  };
};

// Function to analyze resume from text
export const analyzeResume = async (resumeText: string, jobDescription?: string): Promise<ResumeAnalysis> => {
  const response = await fetch(`${API_BASE_URL}/resume/analyze-text`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ 
      text: resumeText, 
      jobDescription: jobDescription ?? null, 
    }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Lỗi phân tích CV');
  }

  const result = await response.json();
  return result.data;
};

// Function for job description analysis
export const analyzeJobDescription = async (jobDescription: string, resumeText: string): Promise<{ score: number; analysis: string }> => {
  const response = await fetch(`${API_BASE_URL}/resume/job-match`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ 
      jobDescription, 
      resumeText 
    }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Lỗi phân tích job match');
  }

  const result = await response.json();
  const data: JobMatchResult = result.data;
  
  return {
    score: data.score,
    analysis: `Dựa trên phân tích AI, CV của bạn phù hợp ${data.score.toFixed(1)}% với yêu cầu công việc.
    
${data.analysis}

Điểm mạnh:
${data.strengths}

Cần cải thiện:
${data.improvements}`
  };
};

// Function to generate interview questions
export const generateInterviewQuestions = async (jobDescription?: string, resumeText?: string): Promise<InterviewQuestion[]> => {
  const response = await fetch(`${API_BASE_URL}/resume/interview-questions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ 
      jobDescription: jobDescription || '', 
      resumeText: resumeText || '' 
    }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Lỗi tạo câu hỏi phỏng vấn');
  }

  const result = await response.json();
  return result.data;
};

// Health check function
export const checkBackendHealth = async (): Promise<boolean> => {
  try {
    const response = await fetch(`${API_BASE_URL}/resume/health`);
    return response.ok;
  } catch (error) {
    console.error('Backend health check failed:', error);
    return false;
  }
};

// Legacy compatibility - keeping old geminiApi export for existing components
export const geminiApi = {
  analyzeResume: async (resumeContent: Record<string, string>) => {
    // Convert old format to new format
    const resumeText = Object.values(resumeContent).join('\n');
    const analysis = await analyzeResume(resumeText);
    
    return {
      improvements: analysis.sections.flatMap(section => 
        section.improvements.map(improvement => ({
          section: section.title,
          original: section.content.split('\n')[0] || '',
          suggestion: improvement,
          reason: 'Gợi ý cải thiện từ AI'
        }))
      ),
      improvedContent: resumeContent // Return original for now
    };
  },
  
  analyzeJobDescription: async (resumeContent: Record<string, string>, jobDescription: string) => {
    const resumeText = Object.values(resumeContent).join('\n');
    const result = await analyzeJobDescription(jobDescription, resumeText);
    
    return {
      matches: [
        { skill: 'React', confidence: 0.9 },
        { skill: 'JavaScript', confidence: 0.95 }
      ],
      gaps: [
        { 
          skill: 'GraphQL', 
          importance: 'high' as const,
          suggestion: 'Cần học thêm GraphQL để phù hợp hơn với công việc.'
        }
      ],
      overallMatch: result.score / 100
    };
  },
  
  getInterviewQuestions: async (jobType: string) => {
    const questions = await generateInterviewQuestions(jobType);
    
    return questions.map(q => ({
      question: q.question,
      hint: 'Hãy trả lời một cách tự tin và cụ thể.'
    }));
  },
  
  evaluateAnswer: async (question: string, answer: string) => {
    // Mock evaluation for now
    return {
      evaluation: 'Câu trả lời tốt, có thể cải thiện thêm.',
      improvementPoints: ['Thêm ví dụ cụ thể', 'Sử dụng số liệu để minh họa'],
      score: Math.min(answer.length / 100, 1) * 100
    };
  }
};

// New function to generate interview questions from backend
export const generateInterviewQuestionsFromBackend = async (position: string, field: string, level: string) => {
  const response = await fetch(`${API_BASE_URL}/mock-interview/questions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      position,
      field,
      level
    }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Lỗi tạo câu hỏi phỏng vấn');
  }

  const result = await response.json();
  return result.map((q: any) => ({
    question: q.questionText,
    hint: q.hint
  }));
};

// New function to submit interview answers for evaluation
export const submitInterviewAnswers = async (
  position: string,
  field: string,
  level: string,
  answers: Array<{questionId: number, questionText: string, answerText: string}>
) => {
  const response = await fetch(`${API_BASE_URL}/mock-interview/submit`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      position,
      field,
      level,
      answers
    }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Lỗi đánh giá câu trả lời');
  }

  return await response.json();
};
