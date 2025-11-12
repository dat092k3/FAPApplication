package com.example.fapapplication.utils;

import com.example.fapapplication.entity.Schedule;
import com.example.fapapplication.dto.ScheduleDTO;

import java.util.List;

public class ScheduleValidator {

    /**
     * Validate schedule trước khi lưu
     * Kiểm tra:
     * 1. Trùng ngày + slot + UCS (1 class không thể có 2 lịch học cùng lúc)
     * 2. Trùng ngày + slot + teacher (1 giáo viên không thể dạy nhiều lớp cùng lúc)
     * 3. Trùng ngày + slot + room (1 phòng chỉ dùng cho 1 lớp)
     */
    public static ValidationResult validate(ScheduleDTO newScheduleDTO,
                                            List<ScheduleDTO> existingScheduleDTOs) {

        Schedule newSchedule = newScheduleDTO.getSchedule();

        for (ScheduleDTO existingDTO : existingScheduleDTOs) {
            Schedule existing = existingDTO.getSchedule();

            // Skip nếu đang edit chính schedule này
            if (existing.getId() != null && existing.getId().equals(newSchedule.getId())) {
                continue;
            }

            // Chỉ check nếu cùng ngày và cùng slot
            if (existing.getDate().equals(newSchedule.getDate()) &&
                    existing.getSlotId().equals(newSchedule.getSlotId())) {

                // Check 1: Trùng UserClassSubject (1 class không thể học 2 buổi cùng lúc)
                if (existing.getUserClassSubjectId().equals(newSchedule.getUserClassSubjectId())) {
                    return new ValidationResult(false,
                            "Class " + newScheduleDTO.getClassName() + " already has " +
                                    existingDTO.getSubjectName() + " at this time!");
                }

                // Check 2: Trùng phòng
                if (existing.getRoom().equals(newSchedule.getRoom())) {
                    return new ValidationResult(false,
                            "Room " + newSchedule.getRoom() + " is already occupied by " +
                                    existingDTO.getClassName() + " at this time!");
                }

                // Check 3: Trùng giáo viên (cần so sánh teacherId từ UserClassSubject)
                // Sẽ được check trong AdminScheduleActivity khi load data
            }
        }

        return new ValidationResult(true, "");
    }

    /**
     * Kiểm tra trùng teacher (cần thêm teacherId vào ScheduleDTO)
     */
    public static boolean checkTeacherConflict(String teacherId, String date, String slotId,
                                               List<ScheduleDTO> existingSchedules,
                                               String currentScheduleId) {
        for (ScheduleDTO dto : existingSchedules) {
            if (dto.getSchedule().getId().equals(currentScheduleId)) {
                continue;
            }

            // Cần implement: lấy teacherId từ UserClassSubject
            // Tạm thời return false
        }
        return false;
    }
}