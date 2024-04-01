package kz.csi.test_task.repository;

import kz.csi.test_task.entity.Autopart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutopartRepository extends JpaRepository<Autopart, Long> {
    List<Autopart> findByParent(Autopart parent);
}
