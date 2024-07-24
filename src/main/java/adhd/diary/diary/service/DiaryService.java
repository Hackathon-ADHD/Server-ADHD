package adhd.diary.diary.service;

import adhd.diary.diary.domain.Diary;
import adhd.diary.diary.domain.DiaryRepository;
import adhd.diary.diary.dto.request.DiaryRequest;
import adhd.diary.diary.dto.response.DiaryResponse;
import adhd.diary.member.domain.Member;
import adhd.diary.member.domain.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public DiaryService(DiaryRepository diaryRepository, MemberRepository memberRepository) {
        this.diaryRepository = diaryRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public DiaryResponse save(DiaryRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));

        Diary diary = request.toEntity();
        diary.setMember(member);
        diary = diaryRepository.save(diary);

        return new DiaryResponse(diary);
    }

    @Transactional
    public List<DiaryResponse> findAll() {
        List<Diary> diaries = diaryRepository.findAll();

        return diaries.stream().map(DiaryResponse::new).toList();
    }
    @Transactional(readOnly = true)
    public DiaryResponse findById(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("일기장 정보를 찾을 수 없습니다."));
        authorizePostMember(diary);
        return new DiaryResponse(diary);
    }

    @Transactional
    public void deleteById(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("일기장 정보를 찾을 수 없습니다."));
        authorizePostMember(diary);

        diaryRepository.deleteById(id);
    }

    @Transactional
    public DiaryResponse updateById(Long id, DiaryRequest request) {
        Diary existingDiary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("일기장 정보를 찾을 수 없습니다."));
        authorizePostMember(existingDiary);

        existingDiary.setContent(request.content());
        existingDiary.setEmotion(request.emotion());

        Diary diary = diaryRepository.save(existingDiary);

        return new DiaryResponse(diary);
    }

    private static void authorizePostMember(Diary diary) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if(!diary.getMember().getEmail().equals(email)){
            throw new IllegalArgumentException("권한이 없습니다.");
        }
    }
}
