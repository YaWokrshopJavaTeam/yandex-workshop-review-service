package ru.practicum.workshop.reviewservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.workshop.reviewservice.model.User;


public interface UserStorage extends JpaRepository<User, Long> {

}
