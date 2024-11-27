package ru.practicum.workshop.reviewservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.workshop.reviewservice.model.User;

@Repository
public interface UserStorage extends JpaRepository<User, Long> {

}
