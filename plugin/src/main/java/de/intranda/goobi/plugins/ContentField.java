package de.intranda.goobi.plugins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentField {
    private String name;
    private String value;
}
