/**
 * @name             UploadViewController.swift
 * @partof           zucred AG
 * @description
 * @author	 		Vasco Mouta
 * @created			25.10.2015
 *
 * Copyright (c) 2015 zucred AG All rights reserved.
 * This material, including documentation and any related
 * computer programs, is protected by copyright controlled by
 * zucred AG. All rights are reserved. Copying,
 * including reproducing, storing, adapting or translating, any
 * or all of this material requires the prior written consent of
 * zucred AG. This material also contains confidential
 * information which may not be disclosed to others without the
 * prior written consent of zucred AG.
 */

import UIKit

public enum OriginView {
    case home
    case jokes
}

open class UploadViewController: TrackUIViewController {
    
    enum TextFields: Int {
        case title = 0
        case tags = 1
    }
    
    open var joke:Joke? = nil
    
    open var origin: OriginView = .home
    
    open var logo: UIImage?
    
    @IBOutlet weak var jokeTitle: UITextField!
    @IBOutlet weak var jokeTags: UITextField!
    
    @IBOutlet weak var countryButtonEN: UIButton!
    @IBOutlet weak var countryButtonCH: UIButton!
    @IBOutlet weak var countryButtonDE: UIButton!
    @IBOutlet weak var countryButtonFR: UIButton!
    @IBOutlet weak var countryButtonIT: UIButton!
    @IBOutlet weak var countryButtonES: UIButton!
    @IBOutlet weak var countryButtonPT: UIButton!
    
    @IBOutlet weak var leadingSwitch: UISwitch!
    @IBOutlet weak var posSwitch: UISwitch!
    
    @IBOutlet weak var tagLove: UIButton!
    @IBOutlet weak var tagSex: UIButton!
    @IBOutlet weak var tagCelebrities: UIButton!
    @IBOutlet weak var tagSports: UIButton!
    @IBOutlet weak var tagNasty: UIButton!
    @IBOutlet weak var tagFood: UIButton!
    @IBOutlet weak var tagGeeks: UIButton!
    @IBOutlet weak var tagAnimals: UIButton!
    
    @IBOutlet weak var cancelBarButton: UIBarButtonItem!
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        let imageView = UIImageView(image:logo)
        self.navigationItem.titleView = imageView
        self.navigationController?.navigationBar.tintColor = UIColor.white
        if origin == .home {
            self.navigationItem.leftBarButtonItem = cancelBarButton
        } else {
            self.navigationItem.leftBarButtonItem = nil
        }
        
        jokeTitle.text = joke?.title
        jokeTitle.tag = TextFields.title.rawValue
        leadingSwitch.isOn = joke?.framingBegin == 1
        posSwitch.isOn = joke?.framingEnd == 1
        if let tags = joke?.tags {
            jokeTags.text = setTags(tags.components(separatedBy: " "))
            jokeTags.tag = TextFields.tags.rawValue
        }
        if let language = joke?.language {
            self.setCountry(language)
        } else if let country = JokrAppDelegate.sharedInstance.login?.getUser()?.country {
            self.setCountry(country)
        }
    }

    override open func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func headHome() {
        stopEditing()
        if let uploadJoke = joke {
            uploadJoke.title = jokeTitle.text!
            uploadJoke.tags = self.selectedTags().joined(separator: " ") + jokeTags.text!
            uploadJoke.language = self.selectedCountry()
            uploadJoke.framingBegin = leadingSwitch.isOn ? 1 : 0
            uploadJoke.framingEnd = posSwitch.isOn ? 1 : 0
        }
        self.performSegue(withIdentifier: "UPLOAD", sender: self)
    }
    
    @IBAction func selectTag(_ sender: UIButton){
        sender.isSelected = !sender.isSelected
    }
    
    func stopEditing() {
        jokeTitle.resignFirstResponder()
        jokeTags.resignFirstResponder()
    }
    
    func setTags(_ tags: [String]) -> String {
        var others: String = ""
        for tag in tags {
            switch(tag) {
            case "#love":
                tagLove.isSelected = true
            case "#sex":
                tagSex.isSelected = true
            case "#celebrities":
                tagCelebrities.isSelected = true
            case "#sports":
                tagSports.isSelected = true
            case "#nasty":
                tagNasty.isSelected = true
            case "#food":
                tagFood.isSelected = true
            case "#geeks":
                tagGeeks.isSelected = true
            case "#animals":
                tagAnimals.isSelected = true
            default:
                others += " " + tag
                break
            }
        }
        return others
    }
    
    func selectedTags() -> [String] {
        var tags: [String] = []
        if tagLove.isSelected {tags.append("#love")}
        if tagSex.isSelected {tags.append("#sex")}
        if tagCelebrities.isSelected {tags.append("#celebrities")}
        if tagSports.isSelected {tags.append("#sports")}
        if tagNasty.isSelected {tags.append("#nasty")}
        if tagFood.isSelected {tags.append("#food")}
        if tagGeeks.isSelected {tags.append("#geeks")}
        if tagAnimals.isSelected {tags.append("#animals")}
        return tags
    }
    
    @IBAction func selectCountry(_ sender: UIButton){
        if countryButtonEN.isSelected {countryButtonEN.isSelected = false}
        else if countryButtonCH.isSelected {countryButtonCH.isSelected = false}
        else if countryButtonDE.isSelected {countryButtonDE.isSelected = false}
        else if countryButtonFR.isSelected {countryButtonFR.isSelected = false}
        else if countryButtonIT.isSelected {countryButtonIT.isSelected = false}
        else if countryButtonES.isSelected {countryButtonES.isSelected = false}
        else if countryButtonPT.isSelected {countryButtonPT.isSelected = false}
        sender.isSelected = !sender.isSelected
    }
    
    
    func setCountry(_ country: String) {
        switch(country) {
        case "en":
            selectCountry(countryButtonEN)
        case "ch":
            selectCountry(countryButtonCH)
        case "de":
            selectCountry(countryButtonDE)
        case "fr":
            selectCountry(countryButtonFR)
        case "it":
            selectCountry(countryButtonIT)
        case "es":
            selectCountry(countryButtonES)
        case "pt":
            selectCountry(countryButtonPT)
        default:
            break
        }
    }
    
    func selectedCountry() -> String {
        if countryButtonEN.isSelected {return "en"}
        else if countryButtonCH.isSelected {return "sch"}
        else if countryButtonDE.isSelected {return "de"}
        else if countryButtonFR.isSelected {return "fr"}
        else if countryButtonIT.isSelected {return "it"}
        else if countryButtonES.isSelected {return "es"}
        else if countryButtonPT.isSelected {return "pt"}
        return "-"
    }
}

// MARK: UITextFieldDelegate

extension UploadViewController: UITextFieldDelegate {
    
    public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder();
        return true;
    }
}
