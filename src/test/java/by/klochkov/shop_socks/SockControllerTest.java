package by.klochkov.shop_socks;

import by.klochkov.shop_socks.dto.SockDto;
import by.klochkov.shop_socks.dto.SockRequestDto;
import by.klochkov.shop_socks.dto.UpdateSockDto;
import by.klochkov.shop_socks.mapper.SockMapper;
import by.klochkov.shop_socks.model.Sock;
import by.klochkov.shop_socks.service.SockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

// Импорты для Spring Test и MockMvc
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;

// Импорты для работы с JSON и сериализации
import com.fasterxml.jackson.databind.ObjectMapper;

// Импорты для MockMvc результатов и проверок
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

// Импорты для Mockito
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;

// Импорты для HTTP статусов и объектов
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

// Импорты для тестирования запросов и ответов
import org.springframework.test.web.servlet.MvcResult;



@SpringBootTest
@AutoConfigureMockMvc
class SockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SockService sockService;

    @MockBean
    private SockMapper sockMapper;

    @Test
    void getSocks_shouldReturnQuantity() throws Exception {
        // Arrange
        String color = "red";
        int minCottonPercentage = 10;
        int maxCottonPercentage = 50;
        int expectedQuantity = 10;

        Mockito.when(sockService.getQuantityWithFilter(color, minCottonPercentage, maxCottonPercentage))
                .thenReturn(expectedQuantity);

        // Act & Assert
        mockMvc.perform(get("/api/socks")
                        .param("color", color)
                        .param("minCottonPercentage", String.valueOf(minCottonPercentage))
                        .param("maxCottonPercentage", String.valueOf(maxCottonPercentage)))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) content().string(String.valueOf(expectedQuantity)));
    }

    @Test
    void getSocks_shouldReturnBadRequestForInvalidParameters() throws Exception {
        mockMvc.perform(get("/api/socks")
                        .param("color", "red")
                        .param("minCottonPercentage", "60")
                        .param("maxCottonPercentage", "50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadBatch_shouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile("content", "socks.csv", "text/csv", "sample data".getBytes());

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) content().string("Данные успешно загружены."));

        Mockito.verify(sockService).processCsvFile(file);
    }

    @Test
    void updateSock_shouldReturnUpdatedSock() throws Exception {
        Long id = 1L;
        UpdateSockDto updateSockDto = new UpdateSockDto("blue", 80, 20);
        Sock updatedSock = new Sock(id, "blue", 80, 20);

        Mockito.when(sockService.updateSock(Mockito.eq(id), Mockito.any(UpdateSockDto.class))).thenReturn(updatedSock);

        mockMvc.perform(put("/api/socks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateSockDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.color").value("blue"));
    }

    @Test
    void registerIncome_shouldReturnSockDto() throws Exception {
        SockRequestDto requestDto = new SockRequestDto("green", 50, 30);
        Sock sock = new Sock(1L, "green", 50, 30);
        SockDto sockDto = new SockDto("green", 50, 30);

        Mockito.when(sockService.registerIncome(requestDto.getColor(), requestDto.getCottonPercentage(), requestDto.getQuantity()))
                .thenReturn(sock);
        Mockito.when(sockMapper.toDto(sock)).thenReturn(sockDto);

        mockMvc.perform(post("/api/socks/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("green"));
    }

    @Test
    void registerOutcome_shouldReturnSuccessMessage() throws Exception {
        SockRequestDto requestDto = new SockRequestDto("black", 40, 10);

        mockMvc.perform(post("/api/socks/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Отпуск носков успешно зарегистрирован."));

        Mockito.verify(sockService).registerOutcome(requestDto.getColor(), requestDto.getCottonPercentage(), requestDto.getQuantity());
    }
}

