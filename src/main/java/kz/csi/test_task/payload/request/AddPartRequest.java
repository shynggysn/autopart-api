package kz.csi.test_task.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPartRequest {
    private Long parentId;
    @NotNull private String detailName;
    @NotNull private Integer price;
    @NotNull private Integer quantity;

}
