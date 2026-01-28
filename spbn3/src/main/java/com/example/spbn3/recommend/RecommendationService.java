package com.example.spbn3.recommend;

import com.example.spbn3.entity.*;
import com.example.spbn3.repository.*;
import com.example.spbn3.service.StudyGroupService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final LearningHistoryRepository historyRepository;
    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final StudyGroupService studyGroupService;

    public RecommendationService(LearningHistoryRepository historyRepository,
                                 TopicRepository topicRepository,
                                 SubjectRepository subjectRepository,
                                 StudyGroupService studyGroupService) {
        this.historyRepository = historyRepository;
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
        this.studyGroupService = studyGroupService;
    }

    // =================================================================
    // 🔥 1. HELPER: CHUẨN HÓA TÊN NGÀNH (DÙNG CHUNG TOÀN FILE)
    // =================================================================
    private String normalizeMajor(String rawMajor) {
        if (rawMajor == null) return "";
        String m = rawMajor.toLowerCase().trim();
        if (m.equals("cntt") || m.equals("it") || m.contains("tin") || m.contains("công nghệ")) {
            return "Công nghệ"; 
        } else if (m.equals("qtkd") || m.contains("kinh tế") || m.contains("quản trị") || m.contains("tài chính")) {
            return "Kinh tế";
        } else if (m.contains("ngôn ngữ") || m.contains("anh") || m.contains("tiếng")) {
            return "Ngôn ngữ";
        }
        return rawMajor;
    }

    // =================================================================
    // 2. DASHBOARD ANALYTICS
    // =================================================================
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardAnalytics(Student student) {
        Map<String, Object> analytics = new HashMap<>();
        Long sId = student.getId();
        String searchKey = normalizeMajor(student.getMajor());

        long totalInMajor = topicRepository.countTotalTopicsByMajor(searchKey);
        long completedInMajor = historyRepository.countCompletedTopicsInMajor(sId, searchKey);
        
        int progress = totalInMajor > 0 ? (int) (completedInMajor * 100 / totalInMajor) : 0;
        analytics.put("progress", Math.min(progress, 100));
        analytics.put("completedCount", completedInMajor);
        analytics.put("streak", calculateStreak(historyRepository.findDistinctLearningDates(sId)));

        // Logic Resume
        List<LearningHistory> recent = historyRepository.findRecentHistory(sId, PageRequest.of(0, 1));
        if (!recent.isEmpty()) {
            Topic lastTopic = recent.get(0).getTopic();
            List<Topic> nextOnes = topicRepository.findNextLogicalTopics(lastTopic.getId(), sId, 1);
            analytics.put("resumeTopic", nextOnes.isEmpty() ? null : nextOnes.get(0));
        } else {
            List<Topic> intro = topicRepository.findIntroTopicsByMajor(searchKey);
            analytics.put("resumeTopic", intro.isEmpty() ? null : intro.get(0));
        }

        List<LearningHistory> histories = historyRepository.findByStudentIdOrderByViewedAtDesc(sId);
        analytics.put("majorRecommendations", getMajorRoadmap(student, histories));
        analytics.put("recommendedGroups", getRecommendedStudyGroups(student)); // Cho Dashboard
        analytics.put("smartSuggestions", getSmartTopicSuggestions(student, histories));

        return analytics;
    }

    // =================================================================
    // 3. GỢI Ý MÔN HỌC (TRANG /SUBJECTS)
    // =================================================================
    public List<Subject> getSubjectRecommendations(Student student) {
        String searchKey = normalizeMajor(student.getMajor());
        List<Subject> recommendations = subjectRepository.findRecommendedSubjects(student.getId(), searchKey, 3);
        if (recommendations.isEmpty()) {
            recommendations = subjectRepository.findGlobalHotSubjects(student.getId(), 3);
        }
        return recommendations;
    }

    // =================================================================
    // 🔥 4. SUBJECT CONTEXT (DÙNG CHO TRANG CHI TIẾT MÔN HỌC)
    // =================================================================
    public Map<String, Object> getSubjectContext(Long studentId, Long subjectId) {
        Map<String, Object> context = new HashMap<>();
        List<Topic> allTopics = topicRepository.findBySubjectIdOrderByIdAsc(subjectId);
        List<LearningHistory> history = historyRepository.findByStudentIdAndSubjectId(studentId, subjectId);
        List<Long> completedIds = history.stream().map(h -> h.getTopic().getId()).collect(Collectors.toList());
        
        int progress = allTopics.isEmpty() ? 0 : (completedIds.size() * 100 / allTopics.size());
        Optional<Topic> nextTopic = allTopics.stream().filter(t -> !completedIds.contains(t.getId())).findFirst();

        context.put("progress", progress);
        context.put("completedIds", completedIds);
        context.put("nextTopic", nextTopic.orElse(null));
        return context;
    }

    // =================================================================
    // 🔥 5. GỢI Ý NHÓM CHO TRANG HISTORY (HỌC GÌ GỢI Ý NẤY)
    // =================================================================
    public List<StudyGroup> getGroupRecommendationsByHistory(Long studentId) {
        List<Subject> learnedSubjects = historyRepository.findLearnedSubjects(studentId);
        if (learnedSubjects.isEmpty()) return new ArrayList<>();

        List<StudyGroup> allGroups = studyGroupService.getAllGroups();
        Set<StudyGroup> matchedGroups = new LinkedHashSet<>();

        for (Subject subject : learnedSubjects) {
            String name = subject.getName().toLowerCase();
            for (StudyGroup group : allGroups) {
                if (group.getSubjectTag() != null) {
                    String tag = group.getSubjectTag().toLowerCase();
                    if (name.contains(tag)) matchedGroups.add(group);
                }
            }
        }
        return new ArrayList<>(matchedGroups);
    }

    // =================================================================
    // 6. GỢI Ý NHÓM THEO NGÀNH (DASHBOARD)
    // =================================================================
    public List<StudyGroup> getRecommendedStudyGroups(Student student) {
        List<StudyGroup> allGroups = studyGroupService.getAllGroups();
        Set<StudyGroup> results = new LinkedHashSet<>();
        if (allGroups == null || student.getMajor() == null) return new ArrayList<>();

        String majorKey = normalizeMajor(student.getMajor());
        Set<String> tags = new HashSet<>();
        tags.add(majorKey.toLowerCase());
        
        if (majorKey.contains("Công nghệ")) {
            tags.addAll(Arrays.asList("cntt", "it", "java", "web", "spring", "python"));
        } else if (majorKey.contains("Kinh tế")) {
            tags.addAll(Arrays.asList("kinh tế", "marketing", "tài chính", "kinh doanh"));
        }

        for (StudyGroup g : allGroups) {
            if (g.getSubjectTag() != null) {
                String gTag = g.getSubjectTag().toLowerCase();
                if (tags.stream().anyMatch(t -> gTag.contains(t) || t.contains(gTag))) {
                    results.add(g);
                }
            }
        }
        return new ArrayList<>(results);
    }

    // =================================================================
    // 7. LỘ TRÌNH NGÀNH HỌC & AI SUGGESTIONS
    // =================================================================
    private List<Topic> getMajorRoadmap(Student student, List<LearningHistory> histories) {
        String searchKey = normalizeMajor(student.getMajor());
        List<Subject> majorSubjects = subjectRepository.findByTargetMajorContainingIgnoreCaseOrderBySemesterAsc(searchKey);
        List<Topic> roadmap = new ArrayList<>();
        for (Subject sub : majorSubjects) {
            if (roadmap.size() >= 4) break;
            boolean learned = histories.stream().anyMatch(h -> h.getTopic().getSubject().getId().equals(sub.getId()));
            if (!learned) topicRepository.findFirstBySubjectIdOrderByIdAsc(sub.getId()).ifPresent(roadmap::add);
        }
        return roadmap;
    }

    private Map<String, List<Topic>> getSmartTopicSuggestions(Student student, List<LearningHistory> histories) {
        Map<String, List<Topic>> suggestions = new LinkedHashMap<>();
        List<Long> completedIds = historyRepository.findCompletedTopicIds(student.getId());
        if (completedIds.isEmpty()) completedIds.add(-1L);
        
        if (!histories.isEmpty()) {
            Long lastTopicId = histories.get(0).getTopic().getId();
            List<Topic> next = topicRepository.findNextLogicalTopics(lastTopicId, student.getId(), 3);
            List<Topic> clean = filterCompleted(next, completedIds);
            if (!clean.isEmpty()) suggestions.put("➡️ Gợi ý tiếp theo", clean);
        }
        return suggestions;
    }

    private List<Topic> filterCompleted(List<Topic> source, List<Long> completedIds) {
        if (source == null) return new ArrayList<>();
        List<Topic> result = new ArrayList<>(source);
        result.removeIf(t -> completedIds.contains(t.getId()));
        return result;
    }

    private int calculateStreak(List<java.sql.Date> dates) {
        if (dates == null || dates.isEmpty()) return 0;
        int streak = 0;
        LocalDate today = LocalDate.now();
        LocalDate expected = null;
        for (java.sql.Date sqlDate : dates) {
            LocalDate current = sqlDate.toLocalDate();
            if (expected == null) {
                if (current.equals(today) || current.equals(today.minusDays(1))) {
                    streak++; expected = current.minusDays(1);
                } else break;
            } else if (current.equals(expected)) {
                streak++; expected = current.minusDays(1);
            } else break;
        }
        return streak;
    }
}