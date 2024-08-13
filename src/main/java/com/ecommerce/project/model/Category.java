package com.ecommerce.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// De class die Spring Boot als een entiteit ziet. Het betreft een POJO (plain old java object) die een representatie is
// van een table in de database. De attributes zijn de columns van de table. Elke instantie van dit object is een rij in
// de tabel
@Entity
// Lombok annotation om de getters/setters/toString en meer te vervangen
// Elk attribuut heeft een getter & setter nodig. Anders kunnen de attributen niet gezet worden in de database en
// ook niet opgehaald worden.
@Data
// Bij het bouwen van een lege database worden de entiteiten geïnstantieerd. Wanneer dit gebeurt, zijn er geen
// properties die meegegeven worden om de attributen mee te vullen. Daarom is een lege constructor nodig
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    // Omdat de gebruiker niet in controle mag zijn bij het definieren van een ID, moet deze automatisch gegenereerd
    // worden. De auto strategy is de meest veilige strategie voor verschillende DBMS. Auto genereert automatisch IDs
    // aan de hand van de records in de database.
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long categoryId;

    // Het verschil tussen NotBlank en NotEmpty is dat NotBlank niet alleen checkt of een veld leeg of null is. Het
    // checkt ook of een veld geen whitespace characters bevat. Onderwater voert het de trim functie uit om alle white
    // space characters te verwijderen.
    @NotBlank
    @Size(min = 5, message = "Category name must contain at least 5 characters")
    private String categoryName;
}
