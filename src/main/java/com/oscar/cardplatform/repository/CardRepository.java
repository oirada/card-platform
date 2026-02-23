package com.oscar.cardplatform.repository;

import com.oscar.cardplatform.domain.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByPanHash(String panHash);

    Optional<Card> findByIdentificador(String identificador);

    Optional<Card> findByIdentificadorAndNumeroValidacion(String identificador, Integer numeroValidacion);
}
