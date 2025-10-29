package com.example.fapapplication.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

/**
 * Một interface CRUD chung để áp dụng cho các DAO khác.
 * @param <T> Kiểu của Entity (ví dụ: User, Product,...)
 */
@Dao
public interface Crud<T> {

    /**
     * Chèn một đối tượng vào database.
     * onConflict = OnConflictStrategy.REPLACE: Nếu đã có đối tượng với khóa chính đó,
     * nó sẽ được thay thế bằng đối tượng mới. Rất hữu ích khi đồng bộ dữ liệu.
     * @param obj Đối tượng cần chèn.
     * @return long ID của hàng vừa được chèn.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(T obj);

    /**
     * Chèn một danh sách đối tượng.
     * @param list Danh sách các đối tượng cần chèn.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(T... list); // Dùng varargs để có thể truyền vào 1 hoặc nhiều đối tượng

    /**
     * Cập nhật một đối tượng đã tồn tại trong database.
     * Room sẽ tìm đối tượng dựa trên khóa chính của nó.
     * @param obj Đối tượng cần cập nhật.
     */
    @Update
    void update(T obj);

    /**
     * Xóa một đối tượng khỏi database.
     * Room sẽ tìm đối tượng dựa trên khóa chính của nó.
     * @param obj Đối tượng cần xóa.
     */
    @Delete
    void delete(T obj);
}
