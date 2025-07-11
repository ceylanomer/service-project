package requests

type CreateServiceRequest struct {
	Resources []Resource `json:"resources"`
}

type Resource struct {
	Id     string  `json:"id"`
	Owners []Owner `json:"owners"`
}

type Owner struct {
	Id            string `json:"id"`
	Name          string `json:"name"`
	AccountNumber string `json:"accountNumber"`
	Level         int    `json:"level"`
}
