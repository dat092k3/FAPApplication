package com.example.fapapplication.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AssessmentCategory model class đại diện cho một danh mục đánh giá trong môn học.
 * Ví dụ: "Practical Exam", "Progress Test", "Project", "Final Exam", v.v.
 *
 * Mỗi category chứa nhiều grade items với weight và điểm số riêng.
 */
public class AssessmentCategory {

    @NonNull
    private String categoryName; // Tên category (PE, PT, Project, FE, etc.)

    @NonNull
    private List<GradeItem> gradeItems; // Danh sách các grade items trong category này

    /**
     * Constructor rỗng bắt buộc cho Firebase
     */
    public AssessmentCategory() {
        this.gradeItems = new ArrayList<>();
    }

    /**
     * Constructor đầy đủ
     *
     * @param categoryName Tên category
     * @param gradeItems Danh sách grade items
     */
    public AssessmentCategory(@NonNull String categoryName, @NonNull List<GradeItem> gradeItems) {
        this.categoryName = categoryName;
        this.gradeItems = gradeItems;
    }

    // === GETTERS ===

    @NonNull
    public String getCategoryName() {
        return categoryName;
    }

    @NonNull
    public List<GradeItem> getGradeItems() {
        return gradeItems;
    }

    // === SETTERS ===

    public void setCategoryName(@NonNull String categoryName) {
        this.categoryName = categoryName;
    }

    public void setGradeItems(@NonNull List<GradeItem> gradeItems) {
        this.gradeItems = gradeItems;
    }

    // === FIREBASE METHODS ===

    /**
     * Chuyển AssessmentCategory object thành Map để lưu vào Firebase
     *
     * @return Map chứa dữ liệu category
     */
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("CategoryName", categoryName);

        // Convert grade items list to list of maps
        List<Map<String, Object>> gradeItemsMaps = new ArrayList<>();
        for (GradeItem item : gradeItems) {
            gradeItemsMaps.add(item.toFirebaseMap());
        }
        map.put("GradeItems", gradeItemsMaps);

        return map;
    }

    /**
     * Tạo AssessmentCategory object từ Firebase DataSnapshot
     *
     * @param snapshot DataSnapshot từ Firebase
     * @return AssessmentCategory object hoặc null nếu data không hợp lệ
     */
    public static AssessmentCategory fromFirebaseSnapshot(DataSnapshot snapshot) {
        try {
            AssessmentCategory category = new AssessmentCategory();

            category.setCategoryName(snapshot.child("CategoryName").getValue(String.class));

            // Parse grade items
            List<GradeItem> gradeItems = new ArrayList<>();
            DataSnapshot gradeItemsSnapshot = snapshot.child("GradeItems");
            for (DataSnapshot itemSnapshot : gradeItemsSnapshot.getChildren()) {
                GradeItem item = GradeItem.fromFirebaseSnapshot(itemSnapshot);
                if (item != null) {
                    gradeItems.add(item);
                }
            }
            category.setGradeItems(gradeItems);

            // Validation
            if (category.getCategoryName() == null) {
                return null;
            }

            return category;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tính tổng weight của tất cả grade items trong category này
     *
     * @return Tổng weight (%)
     */
    @Exclude
    public double getTotalWeight() {
        double total = 0;
        for (GradeItem item : gradeItems) {
            total += item.getWeight();
        }
        return total;
    }

    @Override
    public String toString() {
        return "AssessmentCategory{" +
                "categoryName='" + categoryName + '\'' +
                ", gradeItems=" + gradeItems +
                ", totalWeight=" + getTotalWeight() + "%" +
                '}';
    }
}