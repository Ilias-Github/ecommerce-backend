package com.ecommerce.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// De class die Sping Boot als een entiteit ziet. Het betreft een POJO (plain old java object) die een representatie is
// van een table in de database. De attributes zijn de columns van de table. Elke instantie van dit object is een rij in
// de tabel
@Entity
public class Category {
    // Omdat de gebruiker niet in controle mag zijn bij het definieren van een ID, moet deze automatisch gegenereerd
    // worden. De auto strategy is de meest veilige strategie voor verschillende DBMS. Auto genereert automatisch IDs
    // aan de hand van de records in de database.
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long categoryId;
    private String categoryName;

    // Bij het bouwen van een lege database worden de entiteiten geïnstantieerd. Wanneer dit gebeurt, zijn er geen
    // properties die meegegeven worden om de attributen mee te vullen. Daarom is een lege constructor nodig
    public Category() {
    }

    public Category(Long categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Elk attribuut heeft een getter & setter nodig. Anders kunnen de attributen niet gezet worden in de database en
    // ook niet opgehaald worden.
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
