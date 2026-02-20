package com.example.spbn3.recommend;

import com.example.spbn3.entity.*;
import com.example.spbn3.repository.*;
import com.example.spbn3.service.StudyGroupService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final LearningHistoryRepository historyRepository;
    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final StudyGroupService studyGroupService;
    private final StudentRepository studentRepository; // ĐÃ THÊM

    public RecommendationService(LearningHistoryRepository historyRepository,
                                 TopicRepository topicRepository,
                                 SubjectRepository subjectRepository,
                                 StudyGroupService studyGroupService,
                                 StudentRepository studentRepository) { // ĐÃ THÊM
        this.historyRepository = historyRepository;
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
        this.studyGroupService = studyGroupService;
        this.studentRepository = studentRepository; // ĐÃ THÊM
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
        
        // --- PHẦN THÊM MỚI ---
        // Lấy danh sách xu thế (trendingTopics) để hiển thị ngoài HTML
        List<Topic> trending = topicRepository.findTrendingTopicsByMajor(searchKey, 3);
        if(trending.isEmpty()){
            trending = topicRepository.findTopPopularTopics();
            if(trending.size() > 3) trending = trending.subList(0, 3);
        }
        analytics.put("trendingTopics", trending); 
        // ---------------------

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

    // =================================================================
    // 🔥 8. AI NHẬN DIỆN ĐỘNG LỰC HỌC TẬP (WEIGHTED KNN 3D) 
    // =================================================================
    
    // Lớp nội bộ để tạo các điểm mốc cho AI
    private static class StudentFeatureVector {
        double frequency; // F: Tần suất học 7 ngày qua
        double recency;   // R: Số ngày từ lần cuối học
        double streak;    // S: Chuỗi ngày học liên tục
        String label;     // Trạng thái (Cao, Cháy bỏng...)
        String color;
        String icon;

        public StudentFeatureVector(double f, double r, double s, String label, String color, String icon) {
            this.frequency = f; this.recency = r; this.streak = s;
            this.label = label; this.color = color; this.icon = icon;
        }
    }

    // Tập dữ liệu huấn luyện (Các điểm lý tưởng)
    private List<StudentFeatureVector> generateTrainingDataset() {
        return Arrays.asList(
            new StudentFeatureVector(15, 0, 7, "🔥 Cháy bỏng", "#dc2626", "fas fa-fire"),
            new StudentFeatureVector(10, 0, 5, "🔥 Cháy bỏng", "#dc2626", "fas fa-fire"),
            new StudentFeatureVector(6, 1, 3, "🚀 Tăng tốc", "#ea580c", "fas fa-rocket"),
            new StudentFeatureVector(5, 2, 2, "🚀 Tăng tốc", "#ea580c", "fas fa-rocket"),
            new StudentFeatureVector(2, 2, 1, "🐢 Ổn định", "#16a34a", "fas fa-walking"),
            new StudentFeatureVector(1, 1, 1, "🐢 Ổn định", "#16a34a", "fas fa-walking"),
            new StudentFeatureVector(0, 7, 0, "⚠️ Cần cố gắng", "#f59e0b", "fas fa-exclamation-circle"),
            new StudentFeatureVector(0, 14, 0, "💤 Ngủ đông", "#64748b", "fas fa-bed")
        );
    }

    // Hàm chuẩn hóa dữ liệu
    private double normalize(double value, double min, double max) {
        if (max - min == 0) return 0;
        return (value - min) / (max - min);
    }

    // Hàm chính: Nhận diện Động Lực
    public Map<String, Object> autoDetectMotivation(Long studentId) {
        List<LearningHistory> histories = historyRepository.findByStudentIdOrderByViewedAtDesc(studentId);
        
        // Nếu user chưa học gì
        if (histories.isEmpty()) {
            return Map.of("label", "Khởi động", "color", "#94a3b8", "icon", "fas fa-seedling", "frequency", 0, "streak", 0);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastView = histories.get(0).getViewedAt();
        
        // Trích xuất 3 thông số của sinh viên
        double myRecency = ChronoUnit.DAYS.between(lastView, now);
        double myFrequency = histories.stream().filter(h -> h.getViewedAt().isAfter(now.minusDays(7))).count();
        double myStreak = calculateDetailedStreak(histories);

        List<StudentFeatureVector> dataset = generateTrainingDataset();
        
        // Tìm Max để chuẩn hóa
        double maxF = Math.max(myFrequency, dataset.stream().mapToDouble(v -> v.frequency).max().orElse(20));
        double maxR = Math.max(myRecency, dataset.stream().mapToDouble(v -> v.recency).max().orElse(30));
        double maxS = Math.max(myStreak, dataset.stream().mapToDouble(v -> v.streak).max().orElse(10));

        Map<StudentFeatureVector, Double> distances = new HashMap<>();

        // Tính khoảng cách Euclid
        for (StudentFeatureVector point : dataset) {
            double dF = normalize(myFrequency, 0, maxF) - normalize(point.frequency, 0, maxF);
            double dR = normalize(myRecency, 0, maxR) - normalize(point.recency, 0, maxR);
            double dS = normalize(myStreak, 0, maxS) - normalize(point.streak, 0, maxS);
            distances.put(point, Math.sqrt(dF*dF + dR*dR + dS*dS));
        }

        // Bầu chọn có trọng số (Top 3)
        int K = 3;
        Map<String, Double> weightedVotes = new HashMap<>();
        Map<String, StudentFeatureVector> labelToVectorMap = new HashMap<>();

        distances.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(K)
                .forEach(entry -> {
                    double weight = 1.0 / (entry.getValue() + 0.0001); 
                    weightedVotes.put(entry.getKey().label, weightedVotes.getOrDefault(entry.getKey().label, 0.0) + weight);
                    labelToVectorMap.put(entry.getKey().label, entry.getKey());
                });

        // Chốt kết quả
        String winningLabel = Collections.max(weightedVotes.entrySet(), Map.Entry.comparingByValue()).getKey();
        StudentFeatureVector winningVector = labelToVectorMap.get(winningLabel);

        Map<String, Object> result = new HashMap<>();
        result.put("label", winningVector.label);
        result.put("color", winningVector.color);
        result.put("icon", winningVector.icon);
        result.put("frequency", (int)myFrequency);
        result.put("streak", (int)myStreak);
        
        return result;
    }

    // Helper tính chuỗi ngày học từ List<LearningHistory>
    private double calculateDetailedStreak(List<LearningHistory> histories) {
        if (histories.isEmpty()) return 0;
        Set<LocalDate> activeDays = histories.stream()
                .map(h -> h.getViewedAt().toLocalDate())
                .collect(Collectors.toSet());
        LocalDate today = LocalDate.now();
        if (!activeDays.contains(today) && !activeDays.contains(today.minusDays(1))) return 0;
        
        double streak = 0;
        LocalDate checkDate = activeDays.contains(today) ? today : today.minusDays(1);
        while (activeDays.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        return streak;
    }

    // =================================================================
    // 🔥 9. AI GỢI Ý NHÓM (KNN COLLABORATIVE FILTERING) - ĐÃ BỔ SUNG
    // =================================================================
    public List<StudyGroup> getKnnGroupRecommendations(Long currentStudentId) {
        // Lấy vector bài học của sinh viên hiện tại
        Set<Long> myTopicIds = historyRepository.findByStudentId(currentStudentId).stream()
                .map(h -> h.getTopic().getId()).collect(Collectors.toSet());
        
        if (myTopicIds.isEmpty()) return new ArrayList<>();

        List<Student> allStudents = studentRepository.findAll();
        Map<Student, Double> similarityScores = new HashMap<>();

        for (Student other : allStudents) {
            if (other.getId().equals(currentStudentId)) continue;
            
            Set<Long> otherTopicIds = historyRepository.findByStudentId(other.getId()).stream()
                    .map(h -> h.getTopic().getId()).collect(Collectors.toSet());
            
            // Công thức Jaccard Similarity (Giao chia Hợp)
            Set<Long> intersection = new HashSet<>(myTopicIds); 
            intersection.retainAll(otherTopicIds);
            
            Set<Long> union = new HashSet<>(myTopicIds); 
            union.addAll(otherTopicIds);
            
            double similarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
            if (similarity > 0) similarityScores.put(other, similarity);
        }

        // Top 5 Hàng xóm giống bạn nhất
        List<Student> kNearest = similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Student, Double>comparingByValue().reversed())
                .limit(5).map(Map.Entry::getKey).collect(Collectors.toList());

        Set<StudyGroup> recommendations = new LinkedHashSet<>();
        List<StudyGroup> allGroups = studyGroupService.getAllGroups();
        
        // Lấy nhóm của 5 hàng xóm này để gợi ý cho bạn
        for (Student neighbor : kNearest) {
            for (StudyGroup g : allGroups) {
                boolean neighborJoined = g.getParticipants().stream().anyMatch(s -> s.getId().equals(neighbor.getId()));
                boolean iJoined = g.getParticipants().stream().anyMatch(s -> s.getId().equals(currentStudentId));
                if (neighborJoined && !iJoined) recommendations.add(g);
            }
        }
        return new ArrayList<>(recommendations);
    }
}