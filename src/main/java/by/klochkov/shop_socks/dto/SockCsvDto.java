package by.klochkov.shop_socks.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SockCsvDto {
    @CsvBindByName(column = "color")
    @NotBlank(message = "Цвет не может быть пустым.")
    private String color;

    @CsvBindByName(column = "cottonPercentage")
    @NotNull(message = "Процент хлопка не может быть пустым.")
    @Min(value = 0, message = "Процент хлопка не может быть меньше 0.")
    @Max(value = 100, message = "Процент хлопка не может быть больше 100.")
    private Integer cottonPercentage;

    @CsvBindByName(column = "quantity")
    @NotNull(message = "Количество не может быть пустым.")
    @Min(value = 1, message = "Количество должно быть больше 0.")
    private Integer quantity;
}
