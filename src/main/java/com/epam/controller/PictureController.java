package com.epam.controller;

import com.epam.model.PictureMetadata;
import com.epam.service.PictureService;
import com.epam.service.dto.PictureDto;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@AllArgsConstructor
public class PictureController {

   private final PictureService pictureService;

   @GetMapping("/showUploadPictureForm")
   public String showUploadForm() {
      return "uploadPicture";
   }

   @PostMapping("/uploadPicture")
   public String uploadPicture(@RequestParam("file") MultipartFile file) {
      PictureMetadata pictureMetadata = pictureService.save(file);
      return "redirect:/viewPicture?fileName=" + pictureMetadata.getName();
   }

   @GetMapping("/showFindPictureForm")
   public String showFindPictureForm() {
      return "findPicture";
   }

   @GetMapping("/viewPicture")
   public String viewPicture(@RequestParam("fileName") String fileName, Model model) {
      PictureDto picture = pictureService.getPictureByName(fileName);
      model.addAttribute("picture", picture);
      return "viewPicture";
   }


   @GetMapping("/random")
   public String findRandom(Model model) {
      PictureDto picture = pictureService.findRandom();
      model.addAttribute("picture", picture);
      return "viewPicture";
   }

   @GetMapping("/allPictures")
   public String showAllPictures(Model model) {
      List<PictureDto> pictures = pictureService.getAllPictures();
      model.addAttribute("pictures", pictures);
      return "allPictures";
   }

   @PostMapping("/deletePicture")
   public String deletePicture(@RequestParam("fileName") String fileName, Model model) {
      pictureService.deleteByName(fileName);
      return "redirect:/allPictures";
   }

   @PostMapping("/deleteAll")
   public String deleteAllPictures() {
      pictureService.deleteAll();
      return "redirect:/allPictures";
   }

   @PostMapping("/deleteAllFromDb")
   public String deleteAllPicturesFromDb() {
      pictureService.deleteAllFromDb();
      return "redirect:/allPictures";
   }

   @GetMapping(value = "/downloadPicture")
   public ResponseEntity<ByteArrayResource> downloadPicture(@RequestParam("fileName") String fileName) {
      byte[] data = pictureService.getPictureData(fileName);
      ByteArrayResource resource = new ByteArrayResource(data);
      String mimeType = getImageMimeType(fileName);
      return ResponseEntity.ok().contentLength(data.length).header("Content-type", mimeType)
            .header("Content-disposition", "attachment; filename=\"" + fileName + "\"").body(resource);
   }

   private String getImageMimeType(String fileName) {
      String imageExtension = fileName.substring(fileName.lastIndexOf("."));
      imageExtension = imageExtension.toLowerCase();
      imageExtension = imageExtension.substring(1);
      return "image/" + imageExtension;
   }
}
