package com.qa.portal.feedback.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.qa.portal.common.dto.CohortCourseDto;
import com.qa.portal.common.exception.QaPortalBusinessException;
import com.qa.portal.common.exception.QaResourceNotFoundException;
import com.qa.portal.common.persistence.entity.CohortCourseEntity;
import com.qa.portal.common.persistence.entity.TrainerEntity;
import com.qa.portal.common.persistence.repository.CohortCourseRepository;
import com.qa.portal.common.persistence.repository.QaTrainerRepository;
import com.qa.portal.common.util.mapper.BaseMapper;
import com.qa.portal.feedback.persistence.entity.EvalQuestionCategoryResponseEntity;
import com.qa.portal.feedback.persistence.repository.CohortCourseEvaluationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Component
public class GetCohortCoursesForTrainerOperation {

	private final Logger LOGGER = LoggerFactory.getLogger(GetCohortCoursesForTrainerOperation.class);

	private static final String TRAINER_EVALUATION = "Evaluation Trainer";

	private BaseMapper mapper;

	private CohortCourseRepository cohortCourseRepo;

	private CohortCourseEvaluationRepository cohortEvaluationRepo;

	private QaTrainerRepository trainerRepo;

	private Comparator<CohortCourseDto> cohortCourseComparator = (r1, r2) -> r1.getStartDate().isBefore(r2.getEndDate()) ? 1 : -1;

	
	@Autowired
	public GetCohortCoursesForTrainerOperation(BaseMapper mapper,
											   CohortCourseRepository repo,
											   QaTrainerRepository trainerRepo,
											   CohortCourseEvaluationRepository cohortEvaluationRepo) {
		this.mapper = mapper;
		this.cohortCourseRepo = repo;
		this.cohortEvaluationRepo = cohortEvaluationRepo;
		this.trainerRepo = trainerRepo;
	}

	public List<CohortCourseDto> getCohortCoursesForTrainer(String userName) {
		TrainerEntity trainer = trainerRepo.findByUserName(userName).
				orElseThrow(() -> new QaResourceNotFoundException("Trainer does not exist"));
		return cohortCourseRepo.findByTrainer(trainer)
				.stream()
				.map(c -> getCohortCourseDto(c))
				.sorted(cohortCourseComparator)
				.collect(Collectors.toList());
	}
	
	private CohortCourseDto getCohortCourseDto(CohortCourseEntity cohortCourseEntity) {
		CohortCourseDto cohortCourseDto = mapper.mapObject(cohortCourseEntity, CohortCourseDto.class);
		OptionalDouble evaluation = cohortEvaluationRepo.findByCohortCourse(cohortCourseEntity)
								.stream()
								.flatMap(e -> e.getCategoryResponses().stream())
								.filter(cr -> cr.getQuestionCategory().getCategoryName().equals(TRAINER_EVALUATION))
								.map(cr -> getEvaluationResponseValue(cr))
								.filter(s -> !s.equals("N/A"))
								.mapToInt(s -> Integer.valueOf(s))
								.average();
		cohortCourseDto.setAverageKnowledgeRating("N/A");
		evaluation.ifPresent(e -> cohortCourseDto.setAverageKnowledgeRating(new BigDecimal(e).toString()));
		return cohortCourseDto;
	}

	private String getEvaluationResponseValue(EvalQuestionCategoryResponseEntity questionCategoryResponseEntity) {
		return questionCategoryResponseEntity.getQuestionResponses()
				.stream()
				.findFirst()
				.map(qr -> convertResponseValueToIntString(qr.getResponseValues()))
				.orElseThrow(()  -> new QaPortalBusinessException("Error calculating Trainer evaluation"));
	}

	private String convertResponseValueToIntString(String responseValues) {
		try {
			LOGGER.info("Response Values " + responseValues);
			ObjectMapper om = new ObjectMapper();
			TypeFactory typeFactory = om.getTypeFactory();
			List<Integer> values = om.readValue(responseValues, typeFactory.constructCollectionType(List.class, Integer.class));
			if (values.size() > 0) {
				return values.get(0).toString();
			}
			return "N/A";
		}
		catch(Exception e) {
			throw new QaPortalBusinessException("Error calculating Trainer evaluation");
		}
	}
} 





























