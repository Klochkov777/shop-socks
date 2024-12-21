package by.klochkov.shop_socks;

import by.klochkov.shop_socks.dto.UpdateSockDto;
import by.klochkov.shop_socks.exception.NotEnoughQuantityException;
import by.klochkov.shop_socks.mapper.SockMapper;
import by.klochkov.shop_socks.model.Sock;
import by.klochkov.shop_socks.repository.SockRepository;
import by.klochkov.shop_socks.service.SockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SockServiceTest {

    @Mock
    private SockRepository sockRepository;

    @Mock
    private SockMapper sockMapper;

    @InjectMocks
    private SockService sockService;

    @Test
    void registerIncome_existingSock_shouldIncreaseQuantity() {
        String color = "red";
        int cottonPercentage = 40;
        int quantity = 10;

        Sock existingSock = Sock.builder()
                .color(color)
                .cottonPercentage(cottonPercentage)
                .quantity(5)
                .build();

        Mockito.when(sockRepository.findByColorAndCottonPercentage(color, cottonPercentage))
                .thenReturn(Optional.of(existingSock));
        Mockito.when(sockRepository.save(existingSock)).thenReturn(existingSock);

        Sock result = sockService.registerIncome(color, cottonPercentage, quantity);

        assertEquals(15, result.getQuantity());
        Mockito.verify(sockRepository).save(existingSock);
    }

    @Test
    void registerOutcome_notEnoughQuantity_shouldThrowException() {
        String color = "red";
        int cottonPercentage = 40;
        int quantity = 10;

        Sock existingSock = Sock.builder()
                .color(color)
                .cottonPercentage(cottonPercentage)
                .quantity(5)
                .build();

        Mockito.when(sockRepository.findByColorAndCottonPercentage(color, cottonPercentage))
                .thenReturn(Optional.of(existingSock));

        assertThrows(NotEnoughQuantityException.class,
                () -> sockService.registerOutcome(color, cottonPercentage, quantity));
    }

    @Test
    void getQuantityWithFilter_shouldReturnCorrectValue() {
        String color = "red";
        int minCotton = 30;
        int maxCotton = 50;

        Mockito.when(sockRepository.getTotalQuantityByColorAndCottonPercentageRange(color, minCotton, maxCotton))
                .thenReturn(20);

        int result = sockService.getQuantityWithFilter(color, minCotton, maxCotton);

        assertEquals(20, result);
    }

    @Test
    void processCsvFile_emptyFile_shouldThrowException() {
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> sockService.processCsvFile(emptyFile));
    }

    @Test
    void updateSock_existingSock_shouldUpdateFields() {
        Long sockId = 1L;
        UpdateSockDto dto = new UpdateSockDto("blue", 50, 20);

        Sock existingSock = Sock.builder()
                .id(sockId)
                .color("red")
                .cottonPercentage(40)
                .quantity(10)
                .build();

        Mockito.when(sockRepository.findById(sockId)).thenReturn(Optional.of(existingSock));
        Mockito.doAnswer(invocation -> {
            Sock sock = invocation.getArgument(1);
            sock.setColor(dto.color());
            sock.setCottonPercentage(dto.cottonPercentage());
            sock.setQuantity(dto.quantity());
            return null;
        }).when(sockMapper).updateSockFromDto(Mockito.eq(dto), Mockito.any(Sock.class));

        Sock updatedSock = Sock.builder()
                .id(sockId)
                .color(dto.color())
                .cottonPercentage(dto.cottonPercentage())
                .quantity(dto.quantity())
                .build();

        Mockito.when(sockRepository.save(Mockito.any(Sock.class))).thenReturn(updatedSock);

        Sock result = sockService.updateSock(sockId, dto);

        assertEquals(dto.color(), result.getColor());
        assertEquals(dto.cottonPercentage(), result.getCottonPercentage());
        assertEquals(dto.quantity(), result.getQuantity());
        Mockito.verify(sockRepository).save(Mockito.any(Sock.class));
    }
}
