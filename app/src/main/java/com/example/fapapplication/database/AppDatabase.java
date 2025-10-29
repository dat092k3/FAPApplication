// 1. Sửa lại package cho đúng cấu trúc dự án của bạn
package com.example.fapapplication.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.fapapplication.dao.UserDao;
import com.example.fapapplication.entity.User;

// 2. Import đúng lớp User và UserDao của bạn, KHÔNG phải của Firebase


/**
 * Lớp chính để quản lý cơ sở dữ liệu Room.
 * Nó kết nối các Entity (bảng) và các DAO (truy vấn) lại với nhau.
 *
 * @entities: Liệt kê tất cả các lớp Entity mà database này sẽ quản lý.
 *            Nếu bạn có thêm Course.class, Grade.class, hãy thêm chúng vào đây.
 *
 * @version: Phiên bản của database. Tăng số này lên 1 mỗi khi bạn thay đổi
 *           cấu trúc của bất kỳ bảng nào (thêm/xóa cột, đổi tên bảng...).
 *
 * @exportSchema: Nên đặt là false để tránh warning khi build.
 */
// 3. Thay thế lớp User của Firebase bằng lớp User của bạn
@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // 4. Cung cấp một hàm abstract để Room trả về đối tượng DAO tương ứng
    public abstract UserDao userDao();
    // Nếu có các DAO khác, hãy khai báo chúng ở đây:
    // public abstract CourseDao courseDao();

    // 5. Dùng mẫu Singleton để đảm bảo chỉ có một instance của database trong toàn ứng dụng
    // Điều này tránh rò rỉ bộ nhớ và các vấn đề về truy cập đồng thời.
    private static volatile AppDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;

    // ExecutorService để chạy các tác vụ database trên luồng nền
    public static final java.util.concurrent.ExecutorService databaseWriteExecutor =
            java.util.concurrent.Executors.newFixedThreadPool(NUMBER_OF_THREADS);


    public static AppDatabase getDatabase(final Context context) {
        // Double-checked locking để đảm bảo an toàn trong môi trường đa luồng
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "fap_database") // "fap_database" là tên file .db
                            // Tùy chọn: Thêm một migration strategy khi nâng cấp version
                            // .addCallback(sRoomDatabaseCallback) // Có thể thêm callback khi DB được tạo
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
