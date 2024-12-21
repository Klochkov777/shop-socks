package by.klochkov.shop_socks.service;

import by.klochkov.shop_socks.dto.SockCsvDto;
import by.klochkov.shop_socks.dto.UpdateSockDto;
import by.klochkov.shop_socks.exception.DataProcessingException;
import by.klochkov.shop_socks.exception.NotEnoughQuantityException;
import by.klochkov.shop_socks.exception.ResourceNotFoundException;
import by.klochkov.shop_socks.mapper.SockMapper;
import by.klochkov.shop_socks.model.Sock;
import by.klochkov.shop_socks.repository.SockRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SockService {

    private final SockRepository sockRepository;
    private final SockMapper sockMapper;

    @Transactional
    public Sock registerIncome(String color, Integer cottonPercentage, Integer quantity) {
        Sock sock = sockRepository.findByColorAndCottonPercentage(color, cottonPercentage)
                .orElse(Sock.builder()
                        .color(color)
                        .cottonPercentage(cottonPercentage)
                        .quantity(0)
                        .build());
        sock.setQuantity(sock.getQuantity() + quantity);

        Sock savedSock = sockRepository.save(sock);
        log.info("Registered income: {} socks of color {} with {}% cotton", quantity, color, cottonPercentage);
        return savedSock;
    }

    @Transactional
    public Sock registerOutcome(String color, Integer cottonPercentage, Integer quantity) {
        Sock sock = sockRepository.findByColorAndCottonPercentage(color, cottonPercentage)
                .orElseThrow(() -> new ResourceNotFoundException("Socks not found"));

        if (sock.getQuantity() < quantity) {
            throw new NotEnoughQuantityException("Not enough socks in stock");
        }

        sock.setQuantity(sock.getQuantity() - quantity);

        Sock updatedSock = sockRepository.save(sock);
        log.info("Registered outcome: {} socks of color {} with {}% cotton", quantity, color, cottonPercentage);
        return updatedSock;
    }

    public Integer getQuantityWithFilter(String color, Integer minCottonPercentage, Integer maxCottonPercentage) {
        int totalQuantity = sockRepository
                .getTotalQuantityByColorAndCottonPercentageRange(color, minCottonPercentage, maxCottonPercentage);
        log.info("Quantity of socks color {} minCottonPercentage {} maxCottonPercentage {} equal: {}",
                color, minCottonPercentage, maxCottonPercentage, totalQuantity);
        return totalQuantity;
    }

    @Transactional
    public void processCsvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой.");
        }

        List<SockCsvDto> sockDtos = parseCsvFile(file);
        if (sockDtos.isEmpty()) {
            throw new DataProcessingException("Файл пуст или содержит некорректные данные.");
        }

        List<Sock> socks = sockMapper.toListSocks(sockDtos);

        saveOrUpdateSocks(socks);

        log.info("Данные из CSV-файла успешно обработаны и загружены в {}", LocalDateTime.now());
    }

    private List<SockCsvDto> parseCsvFile(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .build();

            try (CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(parser)
                    .build()) {

                List<String[]> lines = csvReader.readAll();
                log.debug("метод parseCsvFile. прочитано lines.size = {}", lines.size());

                return lines.stream()
                        .skip(1)
                        .map(this::mapToDto)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new DataProcessingException("Ошибка чтения файла.");
        } catch (Exception e) {
            throw new DataProcessingException("Ошибка обработки данных из файла.");
        }
    }

    private SockCsvDto mapToDto(String[] fields) {
        if (fields.length != 3) {
            throw new DataProcessingException("Некорректный формат строки: " + Arrays.toString(fields));
        }
        return SockCsvDto.builder()
                .color(fields[0])
                .cottonPercentage(Integer.parseInt(fields[1]))
                .quantity(Integer.parseInt(fields[2]))
                .build();
    }


    private void saveOrUpdateSocks(List<Sock> socks) {
        for (Sock sock : socks) {
            sockRepository.findByColorAndCottonPercentage(sock.getColor(), sock.getCottonPercentage())
                    .ifPresentOrElse(
                            existingSock -> updateSockQuantity(existingSock, sock.getQuantity()),
                            () -> sockRepository.save(sock)
                    );
        }
    }

    private void updateSockQuantity(Sock existingSock, Integer additionalQuantity) {
        existingSock.setQuantity(existingSock.getQuantity() + additionalQuantity);
        sockRepository.save(existingSock);
    }

    @Transactional
    public Sock updateSock(Long id, UpdateSockDto updateSockDto) {
        Optional<Sock> byColorAndCottonPercentage = sockRepository
                .findByColorAndCottonPercentage(updateSockDto.color(), updateSockDto.cottonPercentage());
        if (byColorAndCottonPercentage.isPresent() && byColorAndCottonPercentage.get().getId() != id) {
            throw new IllegalArgumentException(String.format("Уже есть носки с данным цветом и содержанием " +
                    "хлопка под id %s, изменрите количество у них", byColorAndCottonPercentage.get().getId()));
        }
        Sock sock = sockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sock not found with id: " + id));
        sockMapper.updateSockFromDto(updateSockDto, sock);


        Sock updatedSock = sockRepository.save(sock);
        log.info("Updated sock with id {}: color={}, cottonPercentage={}, quantity={}",
                id, updateSockDto.color(), updateSockDto.cottonPercentage(), updateSockDto.quantity());
        return updatedSock;
    }
}
