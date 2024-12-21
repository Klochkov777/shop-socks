package by.klochkov.shop_socks.repository;

import by.klochkov.shop_socks.model.Sock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SockRepository extends JpaRepository<Sock, Long> {

    Optional<Sock> findByColorAndCottonPercentage(String color, Integer cottonPercentage);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) " +
            "FROM Sock s " +
            "WHERE s.color = :color " +
            "AND s.cottonPercentage >= :minCotton " +
            "AND s.cottonPercentage <= :maxCotton")
    int getTotalQuantityByColorAndCottonPercentageRange(
            @Param("color") String color,
            @Param("minCotton") Integer minCotton,
            @Param("maxCotton") Integer maxCotton);
}
