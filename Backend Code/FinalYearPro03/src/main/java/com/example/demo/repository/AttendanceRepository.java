package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.model.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // If the date column is stored as DATE, the derived method may work.
    // Otherwise, using a JPQL query to extract the date part:
    @Query("SELECT a FROM Attendance a WHERE FUNCTION('DATE', a.date) = :date")
    List<Attendance> findByDate(@Param("date") LocalDate date);
    Attendance findByMobnoAndDate(long mobno, LocalDate date);
    List<Attendance> findByDateAndDept(LocalDate date, String dept);
}
