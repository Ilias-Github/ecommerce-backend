package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

// Gebruikt JPA repository built in functions om database CRUD operaties uit te voeren
// Heeft het type object en type van het ID nodig om te functioneren
// JPA genereert de implementatie van deze repository op runtime
// Dat betekent dat de developer zelf geen implementatie hoeft te schrijven in de interface, maar wel de voordelen van
// de JPA kan profiteren
public interface ICategoryRepository extends JpaRepository<Category, Long> {
    // Om custom searches te implementeren moet de devleoper op een specifieke manier aangeven waar naar gezocht moet
    // worden.
    // findBy -> geeft aan dat er iets gevonden moet worden in de database aan de hand van wat erna komt
    // CategoryName -> de naam van een veld binnen het object
    Category findByCategoryName(String categoryName);
}
