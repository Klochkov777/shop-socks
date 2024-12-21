package by.klochkov.shop_socks.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "DTO для обновления параметров носков")
public record UpdateSockDto(
        @Schema(description = "Цвет носков", example = "red")
        @NotBlank(message = "Цвет носков не может быть пустым")
        String color,

        @Schema(description = "Процент содержания хлопка", example = "75")
        @NotNull(message = "Процент содержания хлопка обязателен")
        @Min(value = 0, message = "Процент содержания хлопка не может быть меньше 0")
        @Max(value = 100, message = "Процент содержания хлопка не может быть больше 100")
        Integer cottonPercentage,

        @Schema(description = "Количество носков", example = "50")
        @NotNull(message = "Количество носков обязательно")
        @Min(value = 1, message = "Количество носков должно быть положительным")
        Integer quantity
) {}
