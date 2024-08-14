package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Representeert de Category model in de presentation laag (het request object)
// Als developer hebben wij volledige controle over welke velden van het model wij terug geven aan de client
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long categoryId;
    private String categoryName;

}
