package com.partyplaylist.model;

/** Жанр из справочника. Неизменяемый, поэтому record. */
public record Genre(Integer id, String name) {
}
