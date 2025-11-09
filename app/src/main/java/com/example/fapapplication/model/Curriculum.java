package com.example.project.model;

public class Curriculum {
    private String id;
    private String subjectCode;
    private String term;

    public Curriculum(String id, String subjectCode, String term) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.term = term;
    }

    public String getId() {
        return id;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getTerm() {
        return term;
    }
}
