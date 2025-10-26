package ru.yandex.javacourse.sprint10.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.javacourse.sprint10.exception.ValidationException;
import ru.yandex.javacourse.sprint10.model.Film;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final List<Film> films = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        film.setId(idGenerator.getAndIncrement());
        films.add(film);
        log.info("Добавлен фильм: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId().equals(film.getId())) {
                films.set(i, film);
                log.info("Обновлён фильм с id={}", film.getId());
                return film;
            }
        }
        throw new ValidationException("Фильм с id=" + film.getId() + " не найден");
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films);
    }
}