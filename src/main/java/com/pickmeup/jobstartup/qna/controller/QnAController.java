package com.pickmeup.jobstartup.qna.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickmeup.jobstartup.common.paging.Criteria;
import com.pickmeup.jobstartup.common.paging.PagingResponse;
import com.pickmeup.jobstartup.qna.dto.AnswerDTO;
import com.pickmeup.jobstartup.qna.dto.AnswerFileDTO;
import com.pickmeup.jobstartup.qna.dto.QuestionDTO;
import com.pickmeup.jobstartup.qna.dto.QuestionFileDTO;
import com.pickmeup.jobstartup.qna.service.QnAService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@RequestMapping("/qna")
public class QnAController {

    private final QnAService qnAService;

    @GetMapping("/write")
    public String writeForm(){
        return "/qna/questionWriteForm";
    }

    //일반 qna
    @PostMapping("/write")
    public String write(HttpSession session, QuestionDTO questionDTO, @RequestParam("qFile_orgName") MultipartFile[] multipartFiles) throws Exception {
        Integer intNo = (Integer) session.getAttribute("memberNo");
        if(intNo==null) {
            return "redirect:/login/";
        }
        long memberNo = intNo;
        questionDTO.setMember_no(memberNo);
        questionDTO.setCompany_no(0);
        qnAService.write(questionDTO, multipartFiles);
        return "/qna/writeDone";
    }

    //회원별 QnA List - 관리자랑 구분해서 (관리자는 전체 목록)
    @GetMapping("/list")
    public String list(HttpSession session, Criteria criteria, Model model) throws Exception {
        Integer intNo = (Integer) session.getAttribute("memberNo");
        Integer roleNo = (Integer) session.getAttribute("role");
        if(intNo==null) {
            return "redirect:/login/";
        }
        long memberNo = intNo;
        if(roleNo==3){
            memberNo=0;
        }
        System.out.println(memberNo);
        PagingResponse<QuestionDTO> questionPage = qnAService.getList(memberNo, criteria);
        System.out.println(questionPage.getList().toString());
        model.addAttribute("questionPage", questionPage);
        model.addAttribute("criteria", criteria);
        return "/qna/questionList";
    }

    //Company QnA List
    @PostMapping("/recruiter/list")
    @ResponseBody
    public String companyQnAList(HttpSession session, @RequestParam("company_no") String company_no, Criteria criteria, Model model) throws Exception {
        long companyNo = Long.parseLong(company_no);
        System.out.println(company_no);
        Integer roleNo = (Integer) session.getAttribute("role");
        if (company_no == null || roleNo != 2) {
            return "redirect:/login/";
        }
        PagingResponse<QuestionDTO> questionPage = qnAService.getCompanyQnAList(companyNo, criteria);
        model.addAttribute("questionPage", questionPage);
        model.addAttribute("criteria", criteria);
        return "/recruiter/mypage/recruiterMyPageContentList";
    }

    //Company QnA ListBoard
    @GetMapping("/recruiter/boardList/{company_no}")
    public String companyQnABoardList(HttpSession session, int company_no, Criteria criteria, Model model) throws Exception {
        long companyNo = company_no;
        System.out.println(company_no);
        Integer roleNo = (Integer) session.getAttribute("role");
        if (company_no==0 || roleNo != 2) {
            return "redirect:/login/";
        }
        PagingResponse<QuestionDTO> questionPage = qnAService.getCompanyQnAList(companyNo, criteria);
        model.addAttribute("questionPage", questionPage);
        model.addAttribute("criteria", criteria);
        return "/qna/questionList";
    }
    
    // 답글 달기
    @PostMapping("/answer")
    public String answerWrite(AnswerDTO answerDTO, @RequestParam("aFile_orgName") MultipartFile[] multipartFiles) throws Exception {
        answerDTO.setMember_no(13L);
        qnAService.answerWrite(answerDTO, multipartFiles);
        return "redirect:/qna/list";
    }

    //질문글 수정 - 답글이 달리면 수정 불가
    @GetMapping("/modify/{qNo}")
    public String modifyQuestion(@PathVariable long qNo, Model model) throws Exception {
        QuestionDTO questionDTO = qnAService.getQuestion(qNo);
        ObjectMapper mapper = new ObjectMapper();
        String typeJson = mapper.writeValueAsString(questionDTO.getQ_type());
        String categoryJson = mapper.writeValueAsString(questionDTO.getQ_category());
        String fileListJson = mapper.writeValueAsString(questionDTO.getQuestionFileDTOList());
        model.addAttribute("typeJson", typeJson);
        model.addAttribute("categoryJson", categoryJson);
        model.addAttribute("fileListJson", fileListJson);
        model.addAttribute("questionDTO", questionDTO);
        return "/qna/questionModifyForm";
    }

    @PostMapping("/modify")
    public String modifyQuestion(QuestionDTO questionDTO, @RequestParam("qFile_orgName") MultipartFile[] multipartFiles, @RequestParam(value = "preFileNo", required = false) long[] preFileNo) throws Exception {
        // 기존의 사진을 모두 삭제
        if(preFileNo==null){
            qnAService.modifyQuestion(questionDTO, multipartFiles);
        } else {
            Arrays.sort(preFileNo);
            qnAService.modifyQuestion(questionDTO, multipartFiles, preFileNo);
        }
        return "redirect:/qna/list";
    }

    @GetMapping("/delete/{qNo}")
    public String delete(@PathVariable long qNo) throws Exception {
        qnAService.delete(qNo);
        return "redirect:/qna/list";
    }

    @GetMapping("/modifyAnswer")
    @ResponseBody
    public AnswerDTO modifyAnswer(@RequestParam("qNo") long qNo) throws Exception {
        AnswerDTO answerDTO = qnAService.detailAnswer(qNo);
        return answerDTO;
    }

    @PostMapping("/modifyAnswer")
    public String modifyAnswer(AnswerDTO answerDTO, @RequestParam("aFile_orgName") MultipartFile[] multipartFiles, @RequestParam(value = "preFileNo", required = false) long[] preFileNo) throws Exception {
        // 기존의 사진을 모두 삭제
        if(preFileNo==null){
            qnAService.modifyAnswer(answerDTO, multipartFiles);
        } else {
            Arrays.sort(preFileNo);
            qnAService.modifyAnswer(answerDTO, multipartFiles, preFileNo);
        }
        return "redirect:/qna/list";
    }

    @GetMapping("/deleteAnswer/{aNo}")
    public String deleteAnswer(@PathVariable long aNo) throws Exception {
        qnAService.deleteAnswer(aNo);
        return "redirect:/qna/list";
    }

    @GetMapping("/qfileDownload/{qFile_no}")
    public ResponseEntity<Resource> downloadQFile(@PathVariable("qFile_no") long qFile_no) throws Exception {
        String uploadQPath = "C:/jobStartUp_fileUpload/qna/question/";
        QuestionFileDTO questionFileDTO = qnAService.getQuestionFile(qFile_no);
        String filePath = uploadQPath+questionFileDTO.getQFile_savName();
        UrlResource resource;
        try{
            resource = new UrlResource(Paths.get(filePath).toUri());
        }catch (MalformedURLException e){
            e.getStackTrace();
            throw new RuntimeException("the given URL path is not valid");
        }
        //Header
        String qFile_savName = questionFileDTO.getQFile_savName();
        String encodedQFile_savName = UriUtils.encode(qFile_savName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedQFile_savName + "\"";

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,contentDisposition)
                .body(resource);
    }


    @GetMapping("/afileDownload/{aFile_no}")
    public ResponseEntity<Resource> downloaAFile(@PathVariable("aFile_no") long aFile_no) throws Exception {
        String uploadAPath = "C:/jobStartUp_fileUpload/qna/answer/";
        AnswerFileDTO answerFileDTO = qnAService.getAnswerFile(aFile_no);
        String filePath = uploadAPath+answerFileDTO.getAFile_savName();
        UrlResource resource;
        try{
            resource = new UrlResource(Paths.get(filePath).toUri());
        }catch (MalformedURLException e){
            e.getStackTrace();
            throw new RuntimeException("the given URL path is not valid");
        }
        String aFile_savName = answerFileDTO.getAFile_savName();
        String encodedAFile_savName = UriUtils.encode(aFile_savName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedAFile_savName + "\"";

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,contentDisposition)
                .body(resource);
    }

}