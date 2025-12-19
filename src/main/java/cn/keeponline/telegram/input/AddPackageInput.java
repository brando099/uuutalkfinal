package cn.keeponline.telegram.input;

import lombok.Data;

@Data
public class AddPackageInput {
    private String outId;
    private Integer packageCount;
    private String durationType = "month";// day month
    private Integer duration = 15;
}
