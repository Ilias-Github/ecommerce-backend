package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

// Gebruikt JPA repository built in functions om database CRUD operaties uit te voeren
// Heeft het type object en type van het ID nodig om te functioneren
// JPA genereert de implementatie van deze repository op runtime
// Dat betekent dat de developer zelf geen implementatie hoeft te schrijven in de interface, maar wel de voordelen van
// de JPA kan profiteren
public interface ICategoryRepository extends JpaRepository<Category, Long> {

}
