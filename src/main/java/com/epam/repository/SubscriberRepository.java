package com.epam.repository;

import com.epam.model.Subscriber;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long>, CrudRepository<Subscriber, Long> {

    Subscriber save(Subscriber subscriber);

    List<Subscriber> findAll();

    List<Subscriber> findByEmail(String email);

}
