package com.example.fapapplication.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * GradeItem model class đại diện cho một mục điểm cụ thể trong assessment category.
 * Ví dụ: "PT1", "PT2", "Final Exam", "Listening", v.v.
 *
 * Mỗi grade item có weight (percentage), value (điểm số/10), và comment.
 */
public class GradeItem {

    @NonNull
    private String itemName; // Tên item (PT1, Final Exam, Listening, etc.)

    private double weight; // Tỷ trọng (percentage), ví dụ: 5.0, 10.0, 40.0

    @Nullable
    private Double value; // Điểm số (score/10), null nếu chưa chấm

    @Nullable
    private String comment; // Ghi chú/nhận xét

    /**
     * Constructor rỗng bắt buộc cho Firebase
     */
    public GradeItem() {
    }

    /**
     * Constructor đầy đủ
     *
     * @param itemName Tên item
     * @param weight Tỷ trọng (%)
     * @param value Điểm số (có thể null)
     * @param comment Ghi chú (có thể null)
     */
    public GradeItem(@NonNull String itemName, double weight, @Nullable Double value, @Nullable String comment) {
        this.itemName = itemName;
        this.weight = weight;
        this.value = value;
        this.comment = comment;
    }

    /**
     * Constructor không có điểm và comment (dùng khi tạo template)
     *
     * @param itemName Tên item
     * @param weight Tỷ trọng (%)
     */
    public GradeItem(@NonNull String itemName, double weight) {
        this.itemName = itemName;
        this.weight = weight;
        this.value = null;
        this.comment = null;
    }

    // === GETTERS ===

    @NonNull
    public String getItemName() {
        return itemName;
    }

    public double getWeight() {
        return weight;
    }

    @Nullable
    public Double getValue() {
        return value;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    // === SETTERS ===

    public void setItemName(@NonNull String itemName) {
        this.itemName = itemName;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setValue(@Nullable Double value) {
        this.value = value;
    }

    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }

    // === FIREBASE METHODS ===

    /**
     * Chuyển GradeItem object thành Map để lưu vào Firebase
     *
     * @return Map chứa dữ liệu grade item
     */
    @Exclude
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ItemName", itemName);
        map.put("Weight", weight);
        map.put("Value", value); // null nếu chưa chấm
        map.put("Comment", comment != null ? comment : "");
        return map;
    }

    /**
     * Tạo GradeItem object từ Firebase DataSnapshot
     *
     * @param snapshot DataSnapshot từ Firebase
     * @return GradeItem object hoặc null nếu data không hợp lệ
     */
    public static GradeItem fromFirebaseSnapshot(DataSnapshot snapshot) {
        try {
            GradeItem item = new GradeItem();

            item.setItemName(snapshot.child("ItemName").getValue(String.class));

            // Parse weight
            Object weightObj = snapshot.child("Weight").getValue();
            if (weightObj instanceof Long) {
                item.setWeight(((Long) weightObj).doubleValue());
            } else if (weightObj instanceof Double) {
                item.setWeight((Double) weightObj);
            } else {
                item.setWeight(0.0);
            }

            // Parse value (có thể null)
            Object valueObj = snapshot.child("Value").getValue();
            if (valueObj instanceof Long) {
                item.setValue(((Long) valueObj).doubleValue());
            } else if (valueObj instanceof Double) {
                item.setValue((Double) valueObj);
            }

            item.setComment(snapshot.child("Comment").getValue(String.class));

            // Validation
            if (item.getItemName() == null) {
                return null;
            }

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Kiểm tra xem item này đã được chấm điểm chưa
     *
     * @return true nếu đã có điểm
     */
    @Exclude
    public boolean isGraded() {
        return value != null;
    }

    @Override
    public String toString() {
        return "GradeItem{" +
                "itemName='" + itemName + '\'' +
                ", weight=" + weight + "%" +
                ", value=" + (value != null ? value : "Not graded") +
                ", comment='" + (comment != null ? comment : "") + '\'' +
                '}';
    }
}