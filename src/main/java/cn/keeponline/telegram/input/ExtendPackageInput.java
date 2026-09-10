package cn.keeponline.telegram.input;

import lombok.Data;

import java.util.List;

@Data
public class ExtendPackageInput {
    private List<Long> packageIds;
    private Integer months = 1;
}
