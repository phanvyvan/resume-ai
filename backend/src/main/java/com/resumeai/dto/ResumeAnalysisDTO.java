package com.resumeai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO cho kết quả phân tích hồ sơ xin việc
 */
public class ResumeAnalysisDTO {
    
    @JsonProperty("kinh_nghiem_lam_viec")
    private SectionAnalysisDTO kinhNghiemLamViec;
    
    @JsonProperty("hoc_van")
    private SectionAnalysisDTO hocVan;
    
    @JsonProperty("ky_nang")
    private SectionAnalysisDTO kyNang;

    // Constructors
    public ResumeAnalysisDTO() {}

    public ResumeAnalysisDTO(
        SectionAnalysisDTO kinhNghiemLamViec, 
        SectionAnalysisDTO hocVan, 
        SectionAnalysisDTO kyNang) {
        this.kinhNghiemLamViec = kinhNghiemLamViec;
        this.hocVan = hocVan;
        this.kyNang = kyNang;
    }

    // Getters and Setters
    public SectionAnalysisDTO getKinhNghiemLamViec() {
        return kinhNghiemLamViec;
    }

    public void setKinhNghiemLamViec(SectionAnalysisDTO kinhNghiemLamViec) {
        this.kinhNghiemLamViec = kinhNghiemLamViec;
    }

    public SectionAnalysisDTO getHocVan() {
        return hocVan;
    }

    public void setHocVan(SectionAnalysisDTO hocVan) {
        this.hocVan = hocVan;
    }

    public SectionAnalysisDTO getKyNang() {
        return kyNang;
    }

    public void setKyNang(SectionAnalysisDTO kyNang) {
        this.kyNang = kyNang;
    }
}

/**
 * DTO cho từng phần phân tích (kinh nghiệm, học vấn, kỹ năng)
 */
class SectionAnalysisDTO {
    
    @JsonProperty("noi_dung")
    private String noiDung;
    
    @JsonProperty("de_xuat")
    private String deXuat;
    
    @JsonProperty("ly_do")
    private String lyDo;

    // Constructors
    public SectionAnalysisDTO() {}

    public SectionAnalysisDTO(String noiDung, String deXuat, String lyDo) {
        this.noiDung = noiDung;
        this.deXuat = deXuat;
        this.lyDo = lyDo;
    }

    // Getters and Setters
    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getDeXuat() {
        return deXuat;
    }

    public void setDeXuat(String deXuat) {
        this.deXuat = deXuat;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }
}