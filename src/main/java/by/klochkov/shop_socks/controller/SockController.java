package by.klochkov.shop_socks.controller;

import by.klochkov.shop_socks.dto.SockDto;
import by.klochkov.shop_socks.dto.SockRequestDto;
import by.klochkov.shop_socks.dto.UpdateSockDto;
import by.klochkov.shop_socks.mapper.SockMapper;
import by.klochkov.shop_socks.model.Sock;
import by.klochkov.shop_socks.service.SockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("api/socks")
@RequiredArgsConstructor
public class SockController {

    private static final Logger log = LoggerFactory.getLogger(SockController.class);
    private final SockService sockService;
    private final SockMapper sockMapper;

    @Operation(summary = "Получение количества носков",
            description = "получение количества носков в зависимости от фильтров" +
            "носков к уже имеющемся и добавляются новые позиции")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Integer.class),
                            examples = @ExampleObject(value = "10"))
            }),
            @ApiResponse(responseCode = "400", description = "BadRequest", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"messages\":" +
                                    "\"IllegalArgumentException\"}"))
            }),
            @ApiResponse(responseCode = "404", description = "NotFound", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"messages\":" +
                                    "\"ресурс не найден\"}"))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":500,\"error\":" +
                                    "\"Internal Server Error\",\"messages\":" +
                                    "\"Произошла ошибка на сервере во время выполнения этой операции\"}"))
            })
    })
    @GetMapping
    public ResponseEntity<Integer> getSocks(
            @RequestParam @Schema(description = "Цвет носков", example = "red")
            String color,
            @RequestParam(defaultValue = "0") @Schema(description = "Миинимальный процент содержания хлопка", example = "10")
            Integer minCottonPercentage,
            @RequestParam(defaultValue = "100") @Schema(description = "Максимальный процент содержания хлопка", example = "50")
            Integer maxCottonPercentage) {
        if (minCottonPercentage > maxCottonPercentage || maxCottonPercentage > 100 || minCottonPercentage < 0) {
            throw new IllegalArgumentException("Введены некорректные параметры.");
        }
        int quantity = sockService.getQuantityWithFilter(color, minCottonPercentage, maxCottonPercentage);
        return ResponseEntity.ok(quantity);
    }


    @Operation(summary = "Загруска файла с данными о носках", description = "Загружается файл и добавляются количество " +
            "носков к уже имеющемся и добавляются новые позиции")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MultipartFile.class))
            }),
            @ApiResponse(responseCode = "400", description = "BadRequest", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"messages\":" +
                                    "\"Файл пуст или содержит некорректные данные\"}"))
            }),
            @ApiResponse(responseCode = "404", description = "NotFound", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"messages\":" +
                                    "\"ресурс не найден\"}"))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":500,\"error\":" +
                                    "\"Internal Server Error\",\"messages\":" +
                                    "\"Произошла ошибка на сервере во время выполнения этой операции\"}"))
            })
    })
    @PostMapping("/batch")
    public ResponseEntity<String> uploadBatch(@RequestParam("content") MultipartFile file) {
        log.info("Uploading file: {}", file.getOriginalFilename());
        sockService.processCsvFile(file);
        return ResponseEntity.ok("Данные успешно загружены.");
    }

    @Operation(summary = "Обновление",
            description = "обновление параметров носков по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Sock.class))
            }),
            @ApiResponse(responseCode = "400", description = "BadRequest", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"messages\":" +
                                    "\"IllegalArgumentException\"}"))
            }),
            @ApiResponse(responseCode = "404", description = "NotFound", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"messages\":" +
                                    "\"ресурс не найден\"}"))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":500,\"error\":" +
                                    "\"Internal Server Error\",\"messages\":" +
                                    "\"Произошла ошибка на сервере во время выполнения этой операции\"}"))
            })
    })
    @PutMapping("/{id}")
    public ResponseEntity<Sock> updateSock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSockDto updateSockDto) {
        Sock updatedSock = sockService.updateSock(id, updateSockDto);
        return ResponseEntity.ok(updatedSock);
    }

    @Operation(summary = "поступление",
            description = "обновление информации при поступлении")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SockDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "BadRequest", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"messages\":" +
                                    "\"Уже есть носки с данным цветом и содержанием хлопка под id 1, " +
                                    "изменрите количество у них\"}"))
            }),
            @ApiResponse(responseCode = "404", description = "NotFound", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"messages\":" +
                                    "\"ресурс не найден\"}"))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":500,\"error\":" +
                                    "\"Internal Server Error\",\"messages\":" +
                                    "\"Произошла ошибка на сервере во время выполнения этой операции\"}"))
            })
    })
    @PostMapping("/income")
    public ResponseEntity<SockDto> registerIncome(
            @RequestBody @Valid SockRequestDto sockRequestDto) {
        Sock sock = sockService.registerIncome(sockRequestDto.getColor(),
                sockRequestDto.getCottonPercentage(), sockRequestDto.getQuantity());
        SockDto dto = sockMapper.toDto(sock);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "отпуск носков",
            description = "обновление информации при отпуске")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))
            }),
            @ApiResponse(responseCode = "400", description = "BadRequest", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"messages\":" +
                                    "\"IllegalArgumentException\"}"))
            }),
            @ApiResponse(responseCode = "404", description = "NotFound", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"messages\":" +
                                    "\"ресурс не найден\"}"))
            }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":500,\"error\":" +
                                    "\"Internal Server Error\",\"messages\":" +
                                    "\"Произошла ошибка на сервере во время выполнения этой операции\"}"))
            })
    })
    @PostMapping("/outcome")
    public ResponseEntity<String> registerOutcome(
            @RequestBody @Valid SockRequestDto sockRequestDto) {
        sockService.registerOutcome(sockRequestDto.getColor(),
                sockRequestDto.getCottonPercentage(), sockRequestDto.getQuantity());
        return ResponseEntity.ok("Отпуск носков успешно зарегистрирован.");
    }
}
