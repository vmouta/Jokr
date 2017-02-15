/**
 * @name             SettingsViewController.swift
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
import zucredApple

open class SettingsTableViewController: TrackUITableViewController {
    
    enum TextFields: Int {
        case name = 0
        case userName = 1
        case email = 2
        case birthday = 3
        case phone = 4
        case website = 5
    }
    
    let sections: [String?] = [nil, "Personal Information", "Languages"]
    
    var imagePicker = UIImagePickerController()
    open var logo: UIImage?
    open var newLogin: Login?
    
    open var login: Login? {
        didSet {
            newLogin = login?.copy() as? Login
        }
    }
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        let imageView = UIImageView(image:logo)
        self.navigationItem.titleView = imageView
        self.navigationController?.navigationBar.tintColor = UIColor.white
        
        if(newLogin!.status == SyncStatus.New.rawValue) {
            navigationItem.leftBarButtonItem = nil
        }
    }
    
    override open func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func addImage(_ sender: AnyObject){
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.savedPhotosAlbum){
            imagePicker.delegate = self
            imagePicker.sourceType = UIImagePickerControllerSourceType.savedPhotosAlbum;
            imagePicker.allowsEditing = false
            self.present(imagePicker, animated: true, completion: nil)
        }
    }
    
    @IBAction func headHome() {
        self.tableView.endEditing(true)
        self.performSegue(withIdentifier: "SETTINGS", sender: self)
    }
}

// MARK: LanguagesCellDelegate

extension SettingsTableViewController: LanguagesCellDelegate {
    
    func countryChange(_ country: String) {
        newLogin?.getUser()?.country = country
        self.tableView.beginUpdates();
        self.tableView.reloadRows(at: [IndexPath(row:0, section:0)], with:.none)
        self.tableView.endUpdates();
    }
    
    func languagesChange(_ languages:[String]) {
        newLogin?.getUser()?.setLangs(languages)
    }
}

// MARK: 
extension SettingsTableViewController {
    
    override public func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    override public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    override public func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return sections[section]
    }
    
    override public func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return (sections[section] == nil ? 1 : super.tableView(tableView, heightForHeaderInSection: section))
    }
    
    override public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        var height: CGFloat
        switch(indexPath.section) {
        case 0:
            height = 110
        case 1:
            height = 110
        case 2:
            height = 250
        default:
            height = 44
            break;
        }
        return height
    }
    
    override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var returnCell: UITableViewCell
        switch(indexPath.section) {
        case 0:
            let cell = tableView.dequeueReusableCell(withIdentifier: "AccountCell", for: indexPath) as! AccountCell
            cell.imageButton.addTarget(self, action: "addImage:", for: .touchUpInside)
            cell.userImage.image = newLogin?.getLoginUserImage()
            cell.setCountry(newLogin?.getUser()?.country ?? "")
            cell.userName.text = newLogin?.getUser()?.name ?? ""
            cell.userName.tag = TextFields.name.rawValue
            cell.userUserName.text = newLogin?.getUser()?.userName ?? ""
            cell.userUserName.tag = TextFields.userName.rawValue
            cell.userEmail.text = newLogin?.getUser()?.email ?? ""
            cell.userEmail.tag = TextFields.email.rawValue
            returnCell = cell
        case 1:
            let cell = tableView.dequeueReusableCell(withIdentifier: "PersonalCell", for: indexPath) as! PersonalCell
            cell.userBirthday.text = newLogin?.getUser()?.birthday ?? ""
            cell.userBirthday.tag = TextFields.birthday.rawValue
            cell.userPhone.text = newLogin?.getUser()?.phone ?? ""
            cell.userPhone.tag = TextFields.phone.rawValue
            cell.userWebsite.text = newLogin?.getUser()?.website ?? ""
            cell.userWebsite.tag = TextFields.website.rawValue
            returnCell = cell
        case 2:
            let cell = tableView.dequeueReusableCell(withIdentifier: "LanguagesCell", for: indexPath) as! LanguagesCell
            cell.setDelegate(self)
            cell.setLanguages(newLogin?.getUser()?.getLangs() ?? [])
            cell.setCountry(newLogin?.getUser()?.country ?? "ch")
            returnCell = cell
        default:
            returnCell = UITableViewCell()
            break;
        }
        return returnCell
    }
}

// Mark: Image Picker

extension SettingsTableViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerController(_ picker: UIImagePickerController!, didFinishPickingImage image: UIImage!, editingInfo: NSDictionary!){
        self.dismiss(animated: true, completion: { () -> Void in
            
        })
        newLogin?.setLoginUserImage(image) { success in
            if success {
                self.tableView.beginUpdates();
                self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow:0, inSection:0)], withRowAnimation:.None)
                self.tableView.endUpdates();
            } else {
                self.showMessage("Fail to store Image", type: .Error, options: nil)
            }
        }
    }
}

// MARK: UITextFieldDelegate

extension SettingsTableViewController: UITextFieldDelegate {

    public func textFieldDidEndEditing(_ textField: UITextField) {
        switch(textField.tag) {
        case TextFields.name.rawValue:
            newLogin?.getUser()?.name = textField.text
        case TextFields.userName.rawValue:
            newLogin?.getUser()?.userName = textField.text
        case TextFields.email.rawValue:
            newLogin?.getUser()?.email = textField.text
        case TextFields.birthday.rawValue:
            newLogin?.getUser()?.birthday = textField.text
        case TextFields.phone.rawValue:
            newLogin?.getUser()?.phone = textField.text
        case TextFields.website.rawValue:
            newLogin?.getUser()?.website = textField.text
        default:
            break
        }
    }
    
    public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder();
        return true;
    }
}

// MARK: Account

class AccountCell: UITableViewCell {
    
    @IBOutlet weak var userUserName: UITextField!
    @IBOutlet weak var userEmail: UITextField!
    @IBOutlet weak var userName: UITextField!
    
    @IBOutlet weak var userImage: UIImageView!
    @IBOutlet weak var imageButton: UIButton!
    @IBOutlet weak var userCountry: UIImageView!
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        userImage.layer.borderWidth = 1
        userImage.layer.masksToBounds = false
        userImage.layer.borderColor = UIColor.white.cgColor
        userImage.layer.cornerRadius = userImage.frame.height/2
        userImage.clipsToBounds = true
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
    func setCountry(_ country: String) {
        switch(country) {
        case "en":
            userCountry.image = UIImage(named:"country_en")
        case "ch":
            userCountry.image = UIImage(named:"country_ch")
        case "de":
            userCountry.image = UIImage(named:"country_de")
        case "fr":
            userCountry.image = UIImage(named:"country_fr")
        case "it":
            userCountry.image = UIImage(named:"country_it")
        case "es":
            userCountry.image = UIImage(named:"country_es")
        case "pt":
            userCountry.image = UIImage(named:"country_pt")
        default:
            break
        }
    }
}


// MARK: Personal Info

class PersonalCell: UITableViewCell {
    
    @IBOutlet weak var userBirthday: UITextField!
    @IBOutlet weak var userPhone: UITextField!
    @IBOutlet weak var userWebsite: UITextField!
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
}


// MARK: Languages

protocol LanguagesCellDelegate {
    func countryChange(_ country: String)
    func languagesChange(_ languages:[String])
}

class LanguagesCell: UITableViewCell {
    
    @IBOutlet weak var countryButtonEN: UIButton!
    @IBOutlet weak var countryButtonCH: UIButton!
    @IBOutlet weak var countryButtonDE: UIButton!
    @IBOutlet weak var countryButtonFR: UIButton!
    @IBOutlet weak var countryButtonIT: UIButton!
    @IBOutlet weak var countryButtonES: UIButton!
    @IBOutlet weak var countryButtonPT: UIButton!
    
    @IBOutlet weak var languageButtonEN: UIButton!
    @IBOutlet weak var languageButtonCH: UIButton!
    @IBOutlet weak var languageButtonDE: UIButton!
    @IBOutlet weak var languageButtonFR: UIButton!
    @IBOutlet weak var languageButtonIT: UIButton!
    @IBOutlet weak var languageButtonES: UIButton!
    @IBOutlet weak var languageButtonPT: UIButton!
    
    var delegate: LanguagesCellDelegate?
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
    
    }
    
    func setDelegate(_ delegate: LanguagesCellDelegate) {
        self.delegate = delegate
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
        delegate?.countryChange(selectedCountry())
    }
    
    @IBAction func selectLanguage(_ sender: UIButton){
        sender.isSelected = !sender.isSelected
        delegate?.languagesChange(selectedLanguges())
    }
    
    func setLanguages(_ languages: [String]) {
        for language in languages {
            switch(language) {
            case "en":
                selectLanguage(languageButtonEN)
            case "sde":
                selectLanguage(languageButtonCH)
            case "de":
                selectLanguage(languageButtonDE)
            case "fr":
                selectLanguage(languageButtonFR)
            case "it":
                selectLanguage(languageButtonIT)
            case "es":
                selectLanguage(languageButtonES)
            case "pt":
                selectLanguage(languageButtonPT)
            default:
                break
            }
        }
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
    
    func selectedLanguges() -> [String] {
        var languages: [String] = []
        if languageButtonEN.isSelected {languages.append("en")}
        if languageButtonCH.isSelected {languages.append("sde")}
        if languageButtonDE.isSelected {languages.append("de")}
        if languageButtonFR.isSelected {languages.append("fr")}
        if languageButtonIT.isSelected {languages.append("it")}
        if languageButtonES.isSelected {languages.append("es")}
        if languageButtonPT.isSelected {languages.append("pt")}
        return languages
    }
    
    func selectedCountry() -> String {
        if countryButtonEN.isSelected {return "en"}
        else if countryButtonCH.isSelected {return "ch"}
        else if countryButtonDE.isSelected {return "de"}
        else if countryButtonFR.isSelected {return "fr"}
        else if countryButtonIT.isSelected {return "it"}
        else if countryButtonES.isSelected {return "es"}
        else if countryButtonPT.isSelected {return "pt"}
        return "-"
    }
}

    
