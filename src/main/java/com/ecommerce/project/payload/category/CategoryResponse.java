package com.ecommerce.project.payload.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// De response die de server terug geeft wanneer de client een lijst aan cagtegories opvraagt
// Dit wordt gedaan aan de hand van de category DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    // De client moet een lijst aan DTO's terugkrijgen zodat de developer controle heeft over welke informatie naar de
    // client wordt verstuurd
    private List<CategoryDTO> content = new ArrayList<>();
    private int pageNumber;
    private int pageSize;
    private Long totalElements;
    private int totalPages;
    private boolean lastPage;
}
