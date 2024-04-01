package kz.csi.test_task.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Autopart {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String detailName;
    private Integer price;
    private Integer quantity;
    private Integer total;

    @JsonIgnore
    @ManyToOne
    private Autopart parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Autopart> children;
    public Autopart(String detailName, Integer price, Integer quantity) {
        this.detailName = detailName;
        this.price = price;
        this.quantity = quantity;
        this.total = price * quantity;
    }

    public Autopart(String detailName, Integer price, Integer quantity, Autopart parent) {
        this.detailName = detailName;
        this.price = price;
        this.quantity = quantity;
        this.total = price * quantity;
        this.parent = parent;
    }
}
