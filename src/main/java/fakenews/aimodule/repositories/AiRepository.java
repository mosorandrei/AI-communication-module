package fakenews.aimodule.repositories;

import fakenews.aimodule.entities.AiResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiRepository extends JpaRepository<AiResultEntity, Integer> {
}
