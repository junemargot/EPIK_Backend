package com.everyplaceinkorea.epik_boot3_api.admin.contents.exhibition.service;

import com.everyplaceinkorea.epik_boot3_api.EditorImage.UploadFolderType;
import com.everyplaceinkorea.epik_boot3_api.admin.contents.exhibition.dto.*;
import com.everyplaceinkorea.epik_boot3_api.entity.Region;
import com.everyplaceinkorea.epik_boot3_api.entity.exhibition.Exhibition;
import com.everyplaceinkorea.epik_boot3_api.entity.exhibition.ExhibitionImage;
import com.everyplaceinkorea.epik_boot3_api.entity.exhibition.ExhibitionTicketOffice;
import com.everyplaceinkorea.epik_boot3_api.entity.exhibition.ExhibitionTicketPrice;
import com.everyplaceinkorea.epik_boot3_api.entity.member.Member;
import com.everyplaceinkorea.epik_boot3_api.repository.Member.MemberRepository;
import com.everyplaceinkorea.epik_boot3_api.repository.RegionRepository;
import com.everyplaceinkorea.epik_boot3_api.repository.exhibition.ExhibitionImageRepository;
import com.everyplaceinkorea.epik_boot3_api.repository.exhibition.ExhibitionRepository;
import com.everyplaceinkorea.epik_boot3_api.repository.exhibition.ExhibitionTicketOfficeRepository;
import com.everyplaceinkorea.epik_boot3_api.repository.exhibition.ExhibitionTicketPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultExhibitionService implements ExhibitionService {

  private final ExhibitionRepository exhibitionRepository;
  private final ExhibitionTicketOfficeRepository exhibitionTicketOfficeRepository;
  private final ExhibitionTicketPriceRepository exhibitionTicketPriceRepository;
  private final MemberRepository memberRepository;
  private final RegionRepository regionRepository;
  private final ModelMapper modelMapper;
  private final ExhibitionImageRepository exhibitionImageRepository;

  @Value("${file.tmp-dir}")
  private String tmpPath;

  @Value("${file.upload-dir}")
  private String uploadPath;

  @Override
  public ExhibitionListDto getList(int page, String keyword, String searchType) {

    // 기본 페이지 번호 설정 - 클라이언트는 1-based 페이지 인덱스를 사용하므로 Spring JPA의 0-based 인덱스에 맞게 변환
    int pageNumber = page - 1;

    // 한 페이지에 표시할 데이터 개수
    int pageSize = 15;

    // 게시물 ID 기준 내림차순 정렬(최신 게시물이 먼저 나오도록 설정)
    Sort sort = Sort.by("id").descending();

    // 페이지 요청 객체 생성
    Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

    // 검색 조건에 맞는 데이터 가져오기
    Page<Exhibition> exhibitionPage = exhibitionRepository.searchExhibition(searchType, keyword, pageable);

    long totalCount = exhibitionPage.getTotalElements(); // 총 게시물 수
    int totalPages = exhibitionPage.getTotalPages(); // 총 페이지 수
    boolean hasPrevPage = exhibitionPage.hasPrevious();
    boolean hasNextPage = exhibitionPage.hasNext();

    List<ExhibitionDto> exhibitionDtos = exhibitionPage.getContent() // 현재 페이지의 데이터 리스트
            .stream()
            .map(exhibition -> {
              ExhibitionDto exhibitionDto = modelMapper.map(exhibition, ExhibitionDto.class);
              exhibitionDto.setWriter(exhibition.getMember().getNickname());

//              log.info(">>> Mapped DTO ID: {}, fileSavedName: {}", exhibition.getId(), exhibitionDto.getF);


              return exhibitionDto;
            }).toList();


    // 페이지 목록 생성 - 현재 페이지를 기준으로 앞뒤 2개의 페이지를 표시하도록 범위 설정
    int currentPage = exhibitionPage.getNumber() + 1;
    List<Long> pages = LongStream.rangeClosed(
            Math.max(1, currentPage - 2),
            Math.min(totalPages, currentPage + 2)
    ).boxed().collect(Collectors.toList());

    // 응답 반환
    return ExhibitionListDto.builder()
            .exhibitionList(exhibitionDtos)
            .totalCount(totalCount)
            .totalPages(totalPages)
            .hasPrev(hasPrevPage)
            .hasNext(hasNextPage)
            .pages(pages)
            .build();

  }

  @Override
  public ExhibitionResponseDto getById(Long id) {
    Exhibition exhibition = exhibitionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("EXHIBITION NOT FOUND WITH ID: " + id));

    List<ExhibitionTicketOffice> exhibitionTicket = exhibitionTicketOfficeRepository.findAllByExhibitionId(id);
    List<ExhibitionTicketPrice> exhibitionPrice = exhibitionTicketPriceRepository.findAllByExhibitionId(id);

    // exhibitionTicket entity -> dto
    // entity를 하나씩 꺼내서 dto로 만든걸
    // exhibitionPrice entity -> dto

    ExhibitionResponseDto exhibitionResponseDto = modelMapper.map(exhibition, ExhibitionResponseDto.class);
    exhibitionResponseDto.setWriter(exhibition.getMember().getNickname()); // 닉네임 설정
    exhibitionResponseDto.setSaveImageName(exhibition.getFileSavedName()); // 이미지 경로 설정 추가 부분

    List<ExhibitionTicketOfficeDto> exhibitionTicketOfficeDtos = new ArrayList<>();
    for(ExhibitionTicketOffice ticketOffice : exhibitionTicket) {
      ExhibitionTicketOfficeDto ticketOfficeDto = modelMapper.map(ticketOffice, ExhibitionTicketOfficeDto.class);

      exhibitionTicketOfficeDtos.add(ticketOfficeDto);
    }
    exhibitionResponseDto.setTicketOffices(exhibitionTicketOfficeDtos); // loop 밖에서 한 번만 설정


    List<ExhibitionTicketPriceDto> exhibitionTicketPriceDtos = new ArrayList<>();
    for(ExhibitionTicketPrice ticketPrice : exhibitionPrice) {
      ExhibitionTicketPriceDto ticketPriceDto = modelMapper.map(ticketPrice, ExhibitionTicketPriceDto.class);

      exhibitionTicketPriceDtos.add(ticketPriceDto);
    }
    exhibitionResponseDto.setTicketPrices(exhibitionTicketPriceDtos); // loop 밖에서 한 번만 설정

    return exhibitionResponseDto;

  }

  @Transactional
  @Override
  public ExhibitionResponseDto create(ExhibitionRequestDto exhibitionRequestDto, MultipartFile files) throws IOException {
      // 인증이 안되니깐 일단 임시로 데이터 넣어놓기
      exhibitionRequestDto.setWriter(1L);
      exhibitionRequestDto.setRegion(1L);

      ExhibitionUploadResultDto uploadResult = uploadFile(files);
      if(uploadResult == null || uploadResult.getFilePath() == null) {
        throw new IllegalArgumentException("FILE UPLOAD FAILED");
      }

      Member member = memberRepository.findById(exhibitionRequestDto.getWriter()).orElseThrow();
      Region region = regionRepository.findById(exhibitionRequestDto.getRegion()).orElseThrow();

      // Exhibition 엔티티 생성
      Exhibition exhibition = modelMapper.map(exhibitionRequestDto, Exhibition.class);

      exhibition.setViewCount(0);
      exhibition.setMember(member);
      exhibition.setRegion(region);
      exhibition.addImage(uploadResult);

      // Exhibition을 저장하고, 그 결과를 savedExhibition에 담는다.
      Exhibition savedExhibition = exhibitionRepository.save(exhibition);

      // 전시회 등록할 때 실제 fileSavedName에 값 들어가는지 확인 -
      // null이면 addImage() 호출 실패, 들어가면 entity에는 잘 들어가고 매핑 시점이 문제임.
      log.info("savedExhibition.fileSavedName = {}", savedExhibition.getFileSavedName());

      List<ExhibitionTicketOffice> exhibitionTicketOffices = saveTicketOffices(exhibitionRequestDto.getExhibitionTicketOffices(), savedExhibition);
      List<ExhibitionTicketPrice> exhibitionTicketPrices = saveTicketPrices(exhibitionRequestDto.getExhibitionTicketPrices(), savedExhibition);

      exhibitionTicketOfficeRepository.saveAll(exhibitionTicketOffices);
      exhibitionTicketPriceRepository.saveAll(exhibitionTicketPrices);

      imageSave(exhibitionRequestDto, savedExhibition);

      ExhibitionResponseDto responseDto = modelMapper.map(savedExhibition, ExhibitionResponseDto.class);
      responseDto.setWriter(member.getNickname());
      responseDto.setSaveImageName(uploadResult.getFileSavedName());

      responseDto.setTicketOffices(exhibitionTicketOffices.stream()
              .map(office -> modelMapper.map(office, ExhibitionTicketOfficeDto.class)).collect(Collectors.toList()));

      responseDto.setTicketPrices(exhibitionTicketPrices.stream()
              .map(price -> modelMapper.map(price, ExhibitionTicketPriceDto.class)).collect(Collectors.toList()));

      log.info("responseDto = {}", responseDto.toString());
      return responseDto;
  }

  private void imageSave(ExhibitionRequestDto exhibitionRequestDto, Exhibition exhibition) throws IOException {
    String[] fileNames = exhibitionRequestDto.getFileNames();

    for (String fileName : fileNames) {
      Path folderPath = Paths.get(System.getProperty("user.dir") + File.separator + tmpPath + File.separator + UploadFolderType.EXHIBITION);
      String fullPath = System.getProperty("user.dir") + File.separator + tmpPath + File.separator + UploadFolderType.EXHIBITION.getFolderName() + fileName;
      Path sourcePath = Paths.get(fullPath);

      log.info("fullPath = {}", fullPath);

      Path targetpath = Paths.get(System.getProperty("user.dir") + File.separator + uploadPath + UploadFolderType.EXHIBITION.getFolderName() + File.separator + fileName);
      if (Files.exists(sourcePath)) {
        log.info("파일명 : {}이 임시폴더에 존재합니다.", fileName);
        // 존재한다면? webapp/image/uplods/musical 로 옮기기
        // 일단 폴더가 존재하는지 확인하고
        if (!Files.exists(folderPath)) {
          Files.createDirectories(folderPath); // 상위 폴더까지 모두 생성
        }
        // 이동할 파일의 현재 경로(sourceDir), 이동 후의 파일 경로 설정(targetDir)
        Files.move(sourcePath, targetpath);
      }

      ExhibitionImage exhibitionImage = ExhibitionImage.builder()
              .fileSaveName(fileName)
              .filePath(fullPath)
              .exhibition(exhibition)
              .build();

      exhibitionImageRepository.save(exhibitionImage);
    }

  }

  @Override
  public ExhibitionResponseDto update(Long id, ExhibitionRequestDto exhibitionRequestDto, MultipartFile file) {
    Exhibition exhibition = exhibitionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("EXHIBITION NOT FOUND"));

    exhibition.setTitle(exhibitionRequestDto.getTitle());
    exhibition.setContent(exhibitionRequestDto.getContent());
    exhibition = exhibitionRepository.save(exhibition);

    ExhibitionResponseDto responseDto = modelMapper.map(exhibition, ExhibitionResponseDto.class);
    responseDto.setWriter(exhibition.getMember().getNickname());

    return responseDto;
  }

  @Override
  public void delete(Long id) {
    Exhibition exhibition = exhibitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당하는 전시회 게시물을 찾을 수 없습니다."));

    // 전시회 이미지 삭제
    exhibitionImageRepository.deleteAllByExhibitionId(id);

    // 전시회 티켓 오피스 삭제
    exhibitionTicketOfficeRepository.deleteByExhibitionId(id);

    // 전시회 티켓 가격 삭제
    exhibitionTicketPriceRepository.deleteByExhibitionId(id);

    // 전시회 게시물 삭제
    exhibitionRepository.deleteById(id);
  }

  @Override
  public void updateExhibitionStatus(Long id) {

  }

  // 전시회 이미지 업로드
  private ExhibitionUploadResultDto uploadFile(MultipartFile file) throws IOException {
    if (file != null && !file.isEmpty()) {
      String originalFilename = file.getOriginalFilename(); // 실제 파일명
      String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      String savedFileName = UUID.randomUUID().toString().replace("-", "") + extension; // uuid.확장자

      File folder = new File(uploadPath + File.separator + UploadFolderType.EXHIBITION.getFolderName());
      if (!folder.exists()) {
        if(!folder.mkdirs()) {
          throw new IllegalArgumentException("이미지 저장 폴더 생성에 실패 하였습니다.");
        }
      }

      String fullPath = folder.getAbsolutePath() + File.separator + savedFileName;
      try {
        file.transferTo(new File(fullPath));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      return new ExhibitionUploadResultDto(savedFileName, fullPath);
    } else {
      return null; // 추후 빈파일이 넘어왔을 경우 예외 처리 필요
    }
  }

  private List<ExhibitionTicketOffice> saveTicketOffices(List<ExhibitionTicketOfficeDto> ticketOffices, Exhibition savedExhibition) {
    if(ticketOffices == null) {
      return new ArrayList<>();
    }

    return ticketOffices.stream()
            .map(dto -> {
              ExhibitionTicketOffice office = new ExhibitionTicketOffice();
              office.setName(dto.getName());
              office.setLink(dto.getLink());
              office.setExhibition(savedExhibition);
              return office;
            }).collect(Collectors.toList());
  }

  private List<ExhibitionTicketPrice> saveTicketPrices(List<ExhibitionTicketPriceDto> ticketPrices, Exhibition savedExhibition) {
    if(ticketPrices == null) {
      return new ArrayList<>();
    }

    return ticketPrices.stream()
            .map(dto -> {
              ExhibitionTicketPrice price = new ExhibitionTicketPrice();
              price.setSeat(dto.getSeat());
              price.setPrice(dto.getPrice());
              price.setExhibition(savedExhibition);
              return price;
            }).collect(Collectors.toList());
  }
}
