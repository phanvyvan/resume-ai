# Resume AI - Ứng dụng Phân tích CV và Mock Interview

Ứng dụng AI hỗ trợ phân tích CV, đối sánh với mô tả công việc và tạo câu hỏi phỏng vấn thử sử dụng Gemini AI.

## Tổng quan

Resume AI được tách thành 2 phần:
- **Frontend**: React + TypeScript + Vite
- **Backend**: Java Spring Boot + Gemini AI

## Cấu trúc dự án

```
profile-wise-upgrade-ai/
├── frontend/                 # React TypeScript frontend
│   ├── src/
│   │   ├── components/      # UI components
│   │   ├── pages/          # Page components
│   │   ├── services/       # API services
│   │   └── hooks/          # Custom hooks
│   ├── public/
│   └── package.json
├── backend/                 # Java Spring Boot backend
│   ├── src/main/java/com/resumeai/
│   │   ├── controller/     # REST controllers
│   │   ├── service/        # Business logic
│   │   ├── model/          # Data models
│   │   └── config/         # Configuration
│   ├── src/main/resources/
│   └── pom.xml
└── README.md               # This file
```

## Tính năng chính

### ✅ Phân tích CV
- Upload file PDF, DOC, DOCX
- Trích xuất text tự động
- Phân tích và đưa ra gợi ý cải thiện
- Hiển thị thông tin được trích xuất
- Chủ động chỉnh sửa thông tin trước khi gửi AI phân tích
- Upload hoặc paste mô tả công việc

### ✅ Mock Interview
- Tạo câu hỏi phỏng vấn dựa trên CV và JD
- Hỗ trợ chọn ngành nghề nếu chưa có CV
- Timer cho từng câu hỏi
- Nhập câu trả lời bằng giọng nói

### ✅ Giao diện responsive
- Thiết kế mobile-first
- UI components hiện đại với Shadcn/UI

## Cài đặt và chạy

### Backend (Java Spring Boot)

```bash
# Di chuyển vào thư mục backend
cd backend

# Cài đặt dependencies và build
mvn clean install

# Cấu hình Gemini API key
export GEMINI_API_KEY="your-gemini-api-key"

# Chạy backend
mvn spring-boot:run
```
Hoặc thay API key vào file `backend/src/main/resources/application.properties`

Backend sẽ chạy tại: `http://localhost:8080`

### Frontend (React + Vite)

```bash
# Di chuyển vào thư mục frontend
cd frontend

# Cài đặt dependencies
npm install

# Chạy development server
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:5173` / `http://localhost:8081`

Nên chạy backend trước khi chạy frontend để tránh bị frontend chiếm port 8080.

## API Endpoints

### Backend API (Port 8080)

- `GET /api/resume/health` - Health check
- `POST /api/resume/upload` - Upload và phân tích CV file
- `POST /api/resume/analyze-text` - Phân tích CV từ text
- `POST /api/resume/job-match` - So sánh CV với JD
- `POST /api/resume/interview-questions` - Tạo câu hỏi phỏng vấn

## Công nghệ sử dụng

### Frontend
- **React 18** với TypeScript
- **Vite** - Build tool và dev server
- **Tailwind CSS** - Styling framework
- **Shadcn/UI** - Component library
- **React Router** - Client-side routing
- **Sonner** - Toast notifications
- **Lucide React** - Icons

### Backend
- **Java 17**
- **Spring Boot 3.2.0** - Web framework
- **Apache POI** - Xử lý file DOC/DOCX
- **Apache PDFBox** - Xử lý file PDF
- **Google Gemini AI** - AI analysis
- **Maven** - Build tool

## Cấu hình

### Backend Configuration

File `backend/src/main/resources/application.properties`:

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# CORS
spring.web.cors.allowed-origins=http://localhost:5173,http://localhost:3000

# Gemini API
gemini.api.key=${GEMINI_API_KEY:your-api-key-here}
gemini.api.model=gemini-2.0-flash

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Frontend Configuration

File `frontend/services/geminiApi.ts` chứa cấu hình API base URL:

```typescript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Development

### Chạy toàn bộ stack

1. **Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run
```

2. **Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

3. Mở browser tại `http://localhost:5173`

### Testing

Backend có health check endpoint để kiểm tra kết nối:
```bash
curl http://localhost:8080/api/resume/health
```

## Deployment

### Backend
```bash
cd backend
mvn clean package
java -jar target/resume-ai-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend
npm run build
# Deploy dist/ folder to web server
```

## Environment Variables

### Required
- `GEMINI_API_KEY` - API key cho Google Gemini AI

### Optional
- `PORT` - Port cho backend (default: 8080)
- `CORS_ORIGINS` - Allowed origins for CORS

## Troubleshooting

### Backend không start
- Kiểm tra Java 17 đã được cài đặt
- Verify Gemini API key
- Check port 8080 có bị conflict không

### Frontend không connect backend
- Đảm bảo backend đang chạy trên port 8080
- Kiểm tra CORS configuration
- Check browser console cho lỗi network

### File upload lỗi
- Kiểm tra file size < 10MB
- Verify file format (PDF, DOC, DOCX)
- Check backend logs cho lỗi parsing

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add some amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.